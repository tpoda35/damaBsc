package org.dama.damajatek.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.game.GameAlreadyFinishedException;
import org.dama.damajatek.exception.game.GameNotFoundException;
import org.dama.damajatek.exception.game.InvalidMoveException;
import org.dama.damajatek.mapper.EventMapper;
import org.dama.damajatek.mapper.GameMapper;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.service.IGameEngine;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.util.BoardInitializer;
import org.dama.damajatek.util.BoardSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.dama.damajatek.enums.game.GameResult.BLACK_WIN;
import static org.dama.damajatek.enums.game.GameResult.RED_WIN;
import static org.dama.damajatek.enums.game.PieceColor.RED;
import static org.dama.damajatek.util.PlayerUtils.isHumanPlayerMatchingUser;
import static org.dama.damajatek.util.PlayerUtils.verifyUserAccess;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final IGameRepository gameRepository;
    private final IAppUserService appUserService;
    private final IGameEngine gameEngine;

    // Rule source: https://www.okosjatek.hu/custom/okosjatek/image/data/srattached/1fb12c3bdc1f524812fb6c5043d11637_D%C3%A1ma%20j%C3%A1t%C3%A9kszab%C3%A1ly.pdf
    // The forced capture rule is used, so if there's a capture, then the user only gets that move.
    // Only the longest capture move is returned.

    @Transactional
    @Override
    public Game createGame(Player redPlayer, Player blackPlayer, Room room) {
        Board board = BoardInitializer.createStartingBoard();

        Game game = Game.builder()
                .room(room)
                .redPlayer(redPlayer)
                .blackPlayer(blackPlayer)
                .build();

        BoardSerializer.saveBoard(game, board);
        return gameRepository.save(game);
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<GameInfoDtoV1> getGameInfo(Long gameId) {
        Game game = findGameByIdWithPlayers(gameId);
        AppUser loggedInUser = appUserService.getLoggedInUser();

        verifyUserAccess(game, loggedInUser);

        PieceColor playerColor =
                isHumanPlayerMatchingUser(game.getRedPlayer(), loggedInUser)
                        ? RED
                        : PieceColor.BLACK;

        Board board = BoardSerializer.loadBoard(game);
        List<Move> validMoves = gameEngine.getAvailableMoves(board, game.getCurrentTurn());

        return CompletableFuture.completedFuture(
                GameMapper.createGameInfoDtoV1(game, board, validMoves, playerColor)
        );
    }

    @Transactional
    @Override
    public List<IGameEvent> makeMove(Long gameId, Move move, Principal principal) {
        // Find the game with the players eagerly loaded
        Game game = findGameByIdWithPlayers(gameId);

        // Auth check
        Authentication auth = (Authentication) principal;
        AppUser loggedInUser = (AppUser) auth.getPrincipal();
        verifyUserAccess(game, loggedInUser);

        // Check if the game is finished
        if (game.isFinished()) {
            throw new GameAlreadyFinishedException();
        }

        // Load the board
        Board board = BoardSerializer.loadBoard(game);
        PieceColor currentTurn = game.getCurrentTurn();

        // Turn validation
        if ((currentTurn == PieceColor.RED && !isHumanPlayerMatchingUser(game.getRedPlayer(), loggedInUser)) ||
                        (currentTurn == PieceColor.BLACK && !isHumanPlayerMatchingUser(game.getBlackPlayer(), loggedInUser))) {
            throw new AccessDeniedException("Not your turn");
        }

        // Validate move
        // Flow:
        // Get all the valid moves and check if there's any move on that list
        // which is equals the sent in move from the player.
        List<Move> validMoves = gameEngine.getAvailableMoves(board, currentTurn);
        Move actualMove = validMoves.stream()
                .filter(m -> m.getFromRow() == move.getFromRow() &&
                        m.getFromCol() == move.getFromCol() &&
                        m.getToRow() == move.getToRow() &&
                        m.getToCol() == move.getToCol())
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Invalid move in game {}: {}", gameId, move);
                    return new InvalidMoveException("Invalid move - forced capture rule violation or move not available");
                });

        // Apply move and check the king promotion
        gameEngine.applyMove(board, actualMove);
        boolean wasPromoted = gameEngine.promoteIfKing(board, actualMove);

        // Update counters (there's a limit how much move can happen in a game)
        // isGameOver method checks these
        game.setTotalMoves(game.getTotalMoves() + 1);
        if (!actualMove.getCapturedPieces().isEmpty() || wasPromoted) {
            game.setMovesWithoutCaptureOrPromotion(0);
        } else {
            game.setMovesWithoutCaptureOrPromotion(game.getMovesWithoutCaptureOrPromotion() + 1);
        }

        // Determine next turn
        PieceColor nextTurn = (currentTurn == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;

        // Save board state
        BoardSerializer.saveBoard(game, board);
        gameRepository.save(game);

        // Create a list for the websocket events
        List<IGameEvent> events = new ArrayList<>();

        // Add the move or capture events
        if (!actualMove.getCapturedPieces().isEmpty()) {
            events.add(EventMapper.createCaptureMadeEvent(actualMove));
        } else {
            events.add(EventMapper.createMoveMadeEvent(actualMove));
        }

        // Add promoted event if there was a promote
        if (wasPromoted) {
            events.add(EventMapper.createPromotedPieceEvent(
                    actualMove,
                    board.getPiece(actualMove.getToRow(), actualMove.getToCol()).getColor()
            ));
        }

        // Check for game over
        boolean gameOver = gameEngine.isGameOver(board, nextTurn, game);

        // If a game is over, send back an event according to the results
        if (gameOver) {
            if (game.getResult() == GameResult.DRAW) {
                events.add(EventMapper.createGameDrawEvent(game.getDrawReason()));
            } else if (game.getWinner() != null) {
                events.add(EventMapper.createGameOverEvent(
                        game.getWinner().getDisplayName(),
                        game.getResult()
                ));
            } else {
                // Defensive fallback
                events.add(EventMapper.createGameDrawEvent());
            }
        } else {
            // Continue the game
            game.setCurrentTurn(nextTurn);
            events.add(EventMapper.createNextTurnEvent(
                    nextTurn,
                    gameEngine.getAvailableMoves(board, nextTurn)
            ));
        }

        gameRepository.save(game);
        log.info("Move executed in game {}", gameId);

        // Return the saved events to the frontend
        return events;
    }

    @Transactional
    @Override
    public IGameEvent forfeit(Long gameId, PieceColor pieceColor) {
        // Find the game with players
        Game game = findGameByIdWithPlayers(gameId);

        // Get the logged-in user
        AppUser loggedInUser = appUserService.getLoggedInUser();

        // Check user access to game
        verifyUserAccess(game, loggedInUser);

        // Check if the game is already finished
        if (game.isFinished()) {
            throw new GameAlreadyFinishedException();
        }

        // Verify that the user is forfeiting their own color
        boolean isUserColor =
                (pieceColor == PieceColor.RED && isHumanPlayerMatchingUser(game.getRedPlayer(), loggedInUser)) ||
                        (pieceColor == PieceColor.BLACK && isHumanPlayerMatchingUser(game.getBlackPlayer(), loggedInUser));

        if (!isUserColor) {
            throw new AccessDeniedException("You can only forfeit your own game");
        }

        // Determine the winner
        Player winner = (pieceColor == PieceColor.RED) ? game.getBlackPlayer() : game.getRedPlayer();
        GameResult result = (pieceColor == PieceColor.RED) ? BLACK_WIN : RED_WIN;

        // Mark game as finished
        game.markFinished(winner, result);
        gameRepository.save(game);

        log.info("Game {} forfeited by {} ({})", gameId, loggedInUser.getDisplayName(), pieceColor);

        // Return game over event
        return EventMapper.createGameForfeitEvent(winner.getDisplayName(), result, "Game forfeited");
    }

    private Game findGameByIdWithPlayers(Long gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> {
                   log.info("Game not found with id {}.", gameId);
                   return new GameNotFoundException();
                });
    }
}