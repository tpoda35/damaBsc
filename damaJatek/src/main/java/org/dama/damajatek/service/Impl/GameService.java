package org.dama.damajatek.service.Impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.IAppUserCacheService;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.game.GameHistoryDto;
import org.dama.damajatek.dto.game.GameInfoDto;
import org.dama.damajatek.dto.game.MoveResult;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.BotPlayer;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.entity.room.Room;
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
import org.dama.damajatek.service.IBotService;
import org.dama.damajatek.service.IGameEngine;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.service.IMoveProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.Principal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.dama.damajatek.enums.game.GameResult.RED_FORFEIT;
import static org.dama.damajatek.enums.game.GameResult.WHITE_FORFEIT;
import static org.dama.damajatek.enums.game.PieceColor.RED;
import static org.dama.damajatek.enums.game.PieceColor.WHITE;
import static org.dama.damajatek.util.BoardInitializer.createStartingBoard;
import static org.dama.damajatek.util.BoardSerializer.loadBoard;
import static org.dama.damajatek.util.BoardSerializer.saveBoard;
import static org.dama.damajatek.util.PlayerUtils.isHumanPlayerMatchingUser;
import static org.dama.damajatek.util.PlayerUtils.verifyUserAccess;

@Slf4j
@Service
public class GameService implements IGameService {

    private final IGameRepository gameRepository;
    private final IAppUserService appUserService;
    private final IGameEngine gameEngine;
    private final IBotService botService;
    private final IMoveProcessor moveProcessor;
    private final IAppUserCacheService appUserCacheService;
    private final TaskScheduler taskScheduler;

    public GameService(
            IGameRepository gameRepository,
            IAppUserService appUserService,
            IGameEngine gameEngine,
            IBotService botService,
            IMoveProcessor moveProcessor,
            IAppUserCacheService appUserCacheService,
            @Qualifier("taskScheduler") TaskScheduler taskScheduler
    ) {
        this.gameRepository = gameRepository;
        this.appUserService = appUserService;
        this.gameEngine = gameEngine;
        this.botService = botService;
        this.moveProcessor = moveProcessor;
        this.appUserCacheService = appUserCacheService;
        this.taskScheduler = taskScheduler;
    }

    // Rule source: https://www.okosjatek.hu/custom/okosjatek/image/data/srattached/1fb12c3bdc1f524812fb6c5043d11637_D%C3%A1ma%20j%C3%A1t%C3%A9kszab%C3%A1ly.pdf
    // The forced capture rule is used, so if there's a capture, then the user only gets that move.
    // Only the longest capture move is returned.

    @Transactional
    @Override
    public Game createGame(Player redPlayer, Player whitePlayer, Room room) {
        Board board = createStartingBoard();

        Game game = Game.builder()
                .room(room)
                .redPlayer(redPlayer)
                .whitePlayer(whitePlayer)
                .build();

        // Save initial board state
        saveBoard(game, board);
        gameRepository.save(game);

        // Add 1s delay to the bot to let the user load the page
        if (game.getCurrentTurn() == RED && redPlayer instanceof BotPlayer) {
            CompletableFuture
                    .delayedExecutor(5, TimeUnit.SECONDS)
                    .execute(() -> botService.playBotTurnAsync(game.getId())); // Make bot move and send the move event through websocket
        }

        return game;
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<GameInfoDto> getGame(Long gameId) {
        Game game = findGameByIdWithPlayers(gameId);
        AppUser loggedInUser = appUserService.getLoggedInUser();

        verifyUserAccess(game, loggedInUser);

        if (game.isFinished()) throw new GameAlreadyFinishedException();

        PieceColor playerColor =
                isHumanPlayerMatchingUser(game.getRedPlayer(), loggedInUser)
                        ? RED
                        : WHITE;

        Board board = loadBoard(game);
        List<Move> validMoves = gameEngine.getAvailableMoves(board, game.getCurrentTurn());

        return CompletableFuture.completedFuture(
                GameMapper.createGameInfoDtoV1(game, board, validMoves, playerColor)
        );
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<Page<GameHistoryDto>> getGameHistory(int pageNum, int pageSize) {
        AppUser loggedInUser = appUserService.getLoggedInUser();

        Page<Game> games = gameRepository.findByPlayerId(
                loggedInUser.getId(),
                PageRequest.of(pageNum, pageSize)
        );

        Page<GameHistoryDto> dtoPage = games.map(game -> {
            OffsetDateTime gameTime = null;
            if (game.getStartTime() != null && game.getEndTime() != null) {
                gameTime = game.getEndTime().minusNanos(game.getStartTime().toInstant().toEpochMilli() * 1_000_000L);
            }

            return GameMapper.createGameHistoryDtoV1(game, gameTime);
        });

        return CompletableFuture.completedFuture(dtoPage);
    }

    // Here I used principal, since this is called with websocket and there's no jwt
    @Transactional
    @Override
    public List<IGameEvent> makeMove(Long gameId, Move move, Principal principal) {
        Game game = findGameByIdWithPlayers(gameId);

        // Auth check
        Authentication auth = (Authentication) principal;
        AppUser loggedInUser = (AppUser) auth.getPrincipal();
        verifyUserAccess(game, loggedInUser);

        if (game.isFinished()) {
            throw new GameAlreadyFinishedException();
        }

        // Load the board
        Board board = loadBoard(game);
        PieceColor currentTurn = game.getCurrentTurn();

        // Turn validation
        if ((currentTurn == RED && !isHumanPlayerMatchingUser(game.getRedPlayer(), loggedInUser)) ||
                (currentTurn == WHITE && !isHumanPlayerMatchingUser(game.getWhitePlayer(), loggedInUser))) {
            throw new AccessDeniedException("Not your turn");
        }

        // Validate the move
        List<Move> validMoves = gameEngine.getAvailableMoves(board, currentTurn);
        Move actualMove = validMoves.stream()
                .filter(m -> m.getFromRow() == move.getFromRow()
                        && m.getFromCol() == move.getFromCol()
                        && m.getToRow() == move.getToRow()
                        && m.getToCol() == move.getToCol())
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Invalid move in game {}: {}", gameId, move);
                    return new InvalidMoveException("Invalid move - forced capture rule violation or move not available");
                });

        // Make move
        MoveResult result = moveProcessor.processMove(game, board, actualMove);

        log.info("Move executed in game {}", gameId);

        // Trigger bot move logic fully async, after the transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                taskScheduler.schedule(
                        () -> botService.playBotTurnAsync(gameId),
                        Instant.now().plusMillis(1800)
                );
            }
        });

        return result.events();
    }

    @Transactional
    @Override
    public IGameEvent forfeit(Long gameId, PieceColor pieceColor) {
        Game game = findGameByIdWithPlayers(gameId);

        AppUser loggedInUser = appUserService.getLoggedInUser();

        verifyUserAccess(game, loggedInUser);

        if (game.isFinished()) {
            throw new GameAlreadyFinishedException();
        }

        // Verify that the user is forfeiting their own color
        boolean isUserColor =
                (pieceColor == RED && isHumanPlayerMatchingUser(game.getRedPlayer(), loggedInUser)) ||
                        (pieceColor == WHITE && isHumanPlayerMatchingUser(game.getWhitePlayer(), loggedInUser));

        if (!isUserColor) {
            throw new AccessDeniedException("You can only forfeit your own game");
        }

        // Determine winner and result
        Player winner = (pieceColor == RED)
                ? game.getWhitePlayer()
                : game.getRedPlayer();

        PieceColor winnerColor = (pieceColor == RED)
                ? WHITE
                : RED;

        GameResult result = (pieceColor == RED)
                ? RED_FORFEIT
                : WHITE_FORFEIT;

        game.markFinished(winner, result);
        gameRepository.save(game);

        log.info("Game {} forfeited by {} ({})", gameId, loggedInUser.getDisplayName(), pieceColor);

        appUserCacheService.evictPlayers(game);

        return EventMapper.createGameForfeitEvent(
                winner.getDisplayName(),
                winnerColor,
                result,
                "Game forfeited"
        );
    }

    private Game findGameByIdWithPlayers(Long gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> {
                    log.info("Game not found with id {}.", gameId);
                    return new GameNotFoundException();
                });
    }
}