package org.dama.damajatek.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.dto.game.websocket.GameEvent;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.enums.game.BotDifficulty;
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
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.repository.GameRepository;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.util.BoardInitializer;
import org.dama.damajatek.util.BoardSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.dama.damajatek.enums.game.GameResult.BLACK_WIN;
import static org.dama.damajatek.enums.game.GameResult.RED_WIN;
import static org.dama.damajatek.enums.game.PieceColor.RED;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final GameRepository gameRepository;
    private final IAppUserService appUserService;

    // Rule source: https://www.okosjatek.hu/custom/okosjatek/image/data/srattached/1fb12c3bdc1f524812fb6c5043d11637_D%C3%A1ma%20j%C3%A1t%C3%A9kszab%C3%A1ly.pdf
    // The forced capture rule is used, so if there's a capture, then the user only gets that move.
    // Only the longest capture move is returned.

    @Transactional
    @Override
    public Game createGame(AppUser redPlayer, AppUser blackPlayer, Room room, boolean vsBot, BotDifficulty difficulty) {
        Board board = BoardInitializer.createStartingBoard();

        Game game = Game.builder()
                .room(room)
                .redPlayer(redPlayer)
                .blackPlayer(blackPlayer)
                .vsBot(vsBot)
                .botDifficulty(difficulty)
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

        checkUserAccessToGame(game, loggedInUser);

        PieceColor playerColor =
                loggedInUser.getId().equals(game.getRedPlayer().getId())
                        ? RED
                        : PieceColor.BLACK;

        Board board = BoardSerializer.loadBoard(game);
        List<Move> validMoves = getAvailableMoves(board, game.getCurrentTurn());

        return CompletableFuture.completedFuture(
                GameMapper.createGameInfoDtoV1(game, board, validMoves, playerColor)
        );
    }

    @Transactional
    @Override
    public List<GameEvent> makeMove(Long gameId, Move move, Principal principal) {
        // Find the game with the players eagerly loaded
        Game game = findGameByIdWithPlayers(gameId);

        // Auth check
        Authentication auth = (Authentication) principal;
        AppUser loggedInUser = (AppUser) auth.getPrincipal();
        checkUserAccessToGame(game, loggedInUser);

        // Check if the game is finished
        if (game.isFinished()) {
            throw new GameAlreadyFinishedException();
        }

        // Load the board
        Board board = BoardSerializer.loadBoard(game);
        PieceColor currentTurn = game.getCurrentTurn();

        // Turn validation
        if ((currentTurn == PieceColor.RED && !loggedInUser.getId().equals(game.getRedPlayer().getId())) ||
                (currentTurn == PieceColor.BLACK && !loggedInUser.getId().equals(game.getBlackPlayer().getId()))) {
            throw new AccessDeniedException("Not your turn");
        }

        // Validate move
        // Flow:
        // Get all the valid moves and check if there's any move on that list
        // which is equals the sent in move from the player.
        List<Move> validMoves = getAvailableMoves(board, currentTurn);
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
        applyMove(board, actualMove);
        boolean wasPromoted = promoteIfKing(board, actualMove);

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
        List<GameEvent> events = new ArrayList<>();

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
        boolean gameOver = isGameOver(board, nextTurn, game);

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
                    getAvailableMoves(board, nextTurn)
            ));
        }

        gameRepository.save(game);
        log.info("Move executed in game {}", gameId);

        // Return the saved events to the frontend
        return events;
    }

    @Transactional
    @Override
    public GameEvent forfeit(Long gameId, PieceColor pieceColor) {
        // Find the game with players
        Game game = findGameByIdWithPlayers(gameId);

        // Get the logged-in user
        AppUser loggedInUser = appUserService.getLoggedInUser();

        // Check user access to game
        checkUserAccessToGame(game, loggedInUser);

        // Check if the game is already finished
        if (game.isFinished()) {
            throw new GameAlreadyFinishedException();
        }

        // Verify that the user is forfeiting their own color
        boolean isUserColor = (pieceColor == PieceColor.RED && loggedInUser.getId().equals(game.getRedPlayer().getId())) ||
                (pieceColor == PieceColor.BLACK && loggedInUser.getId().equals(game.getBlackPlayer().getId()));

        if (!isUserColor) {
            throw new AccessDeniedException("You can only forfeit your own game");
        }

        // Determine the winner
        AppUser winner = (pieceColor == PieceColor.RED) ? game.getBlackPlayer() : game.getRedPlayer();
        GameResult result = (pieceColor == PieceColor.RED) ? BLACK_WIN : RED_WIN;

        // Mark game as finished
        game.markFinished(winner, result);
        gameRepository.save(game);

        log.info("Game {} forfeited by {} ({})", gameId, loggedInUser.getDisplayName(), pieceColor);

        // Return game over event
        return EventMapper.createGameForfeitEvent(winner.getDisplayName(), result, "Game forfeited");
    }

    private void checkUserAccessToGame(Game game, AppUser loggedInUser) {
        Long userId = loggedInUser.getId();

        boolean isBlackPlayer = game.getBlackPlayer() != null &&
                userId.equals(game.getBlackPlayer().getId());
        boolean isRedPlayer = game.getRedPlayer() != null &&
                userId.equals(game.getRedPlayer().getId());

        if (!isBlackPlayer && !isRedPlayer) {
            log.warn("Unauthorized access to game(id: {}) from user(id: {}).", game.getId(), userId);
            throw new AccessDeniedException("You are not a participant in this game");
        }
    }

    // Chains the findAllCaptureMoves and findAllValidMoves.
    // If there's any capture, then only that returns
    // If there's no capture, then the other valid moves returns
    private List<Move> getAvailableMoves(Board board, PieceColor color) {
        List<Move> captureMoves = findAllCaptureMoves(board, color);

        if (!captureMoves.isEmpty()) {
            return captureMoves;
        }

        return findAllValidMoves(board, color);
    }

    // Iterates through the full table, searches for the pieces with the given color.
    // It uses the findValidMovesForPiece method, to find all the possible moves for all the pieces.
    // It only returns normal moves, not captures.
    private List<Move> findAllValidMoves(Board board, PieceColor color) {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    moves.addAll(findValidMovesForPiece(board, row, col));
                }
            }
        }

        return moves;
    }

    private List<Move> findValidMovesForPiece(Board board, int row, int col) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);

        if (piece == null) return moves;

        // Check regular moves
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            // Skip backward moves for regular pieces
            if (isDirectionNotAllowedForPiece(piece, dir)) continue;

            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(
                        Move.builder()
                        .fromRow(row)
                        .fromCol(col)
                        .toRow(newRow)
                        .toCol(newCol)
                        .build()
                );
            }
        }

        return moves;
    }

    // Iterates through the full table, searches for the pieces with the given color.
    // It uses the findCaptureMovesForPiece method, to find all the possible capture moves for all the pieces.
    // It only returns capture moves, not captures.
    private List<Move> findAllCaptureMoves(Board board, PieceColor color) {
        List<Move> captures = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    captures.addAll(findCaptureMovesForPiece(board, row, col));
                }
            }
        }

        return captures;
    }

    private List<Move> findCaptureMovesForPiece(Board board, int row, int col) {
        List<Move> allCaptures = new ArrayList<>();
        Piece piece = board.getPiece(row, col);

        if (piece == null) return allCaptures;

        Move initialMove = Move.builder()
                .fromRow(row)
                .fromCol(col)
                .toRow(row)
                .toCol(col)
                .build();

        // Start the recursive chain capture search
        findChainCapturesRecursive(board, row, col, piece, initialMove, new HashSet<>(), allCaptures);

        // If no chain captures found, return empty list
        if (allCaptures.isEmpty()) {
            return allCaptures;
        }

        // Find the maximum number of captures in any chain
        int maxCaptures = allCaptures.stream()
                .mapToInt(move -> move.getCapturedPieces().size())
                .max()
                .orElse(0);

        // Only return moves with the maximum number of captures
        return allCaptures.stream()
                .filter(move -> move.getCapturedPieces().size() == maxCaptures)
                .collect(Collectors.toList());
    }

    private void findChainCapturesRecursive(
            Board board,
            int currentRow,
            int currentCol,
            Piece originalPiece,
            Move currentMove,
            Set<String> capturedPositions,
            List<Move> allCaptures) {

        boolean foundCapture = false;
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            // Skip backward moves for regular pieces
            if (isDirectionNotAllowedForPiece(originalPiece, dir)) continue;

            int middleRow = currentRow + dir[0];
            int middleCol = currentCol + dir[1];
            int jumpRow = currentRow + dir[0] * 2;
            int jumpCol = currentCol + dir[1] * 2;

            String middleKey = middleRow + "," + middleCol;

            // Check if this is a valid capture
            if (isInBounds(jumpRow, jumpCol) &&
                    isInBounds(middleRow, middleCol) &&
                    board.getPiece(jumpRow, jumpCol) == null &&
                    !capturedPositions.contains(middleKey)) {

                Piece middle = board.getPiece(middleRow, middleCol);

                if (middle != null && middle.getColor() != originalPiece.getColor()) {
                    foundCapture = true;

                    // Create a new move with this capture added
                    Move newMove = Move.builder()
                            .fromRow(currentMove.getFromRow())
                            .fromCol(currentMove.getFromCol())
                            .toRow(jumpRow)
                            .toCol(jumpCol)
                            .build();

                    // Copy all previously captured pieces
                    for (int[] captured : currentMove.getCapturedPieces()) {
                        newMove.getCapturedPieces().add(new int[]{captured[0], captured[1]});
                    }

                    // Add this new captured piece
                    newMove.getCapturedPieces().add(new int[]{middleRow, middleCol});

                    // Copy previous path
                    for (int[] step : currentMove.getPath()) {
                        newMove.getPath().add(new int[]{step[0], step[1]});
                    }

                    newMove.getPath().add(new int[]{jumpRow, jumpCol});

                    // Create new set of captured positions for this branch
                    Set<String> newCaptured = new HashSet<>(capturedPositions);
                    newCaptured.add(middleKey);

                    // Continue searching for more captures from the landing position
                    findChainCapturesRecursive(
                            board,
                            jumpRow,
                            jumpCol,
                            originalPiece,
                            newMove,
                            newCaptured,
                            allCaptures
                    );
                }
            }
        }

        // If no more captures possible from this position, add this as a complete chain
        if (!foundCapture && !currentMove.getCapturedPieces().isEmpty()) {

            // Remove the last element, since we have it in the Move (the toRow, toCol part)
            List<int[]> path = currentMove.getPath();
            if (!path.isEmpty()) {
                int[] last = path.getLast();
                if (last[0] == currentMove.getToRow() && last[1] == currentMove.getToCol()) {
                    path.removeLast();
                }
            }

            allCaptures.add(currentMove);
        }
    }

    // Checks if the given row&col is on the board
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private void applyMove(Board board, Move move) {
        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());
        board.setPiece(move.getToRow(), move.getToCol(), piece);
        board.removePiece(move.getFromRow(), move.getFromCol());

        // Remove captured pieces
        for (int[] captured : move.getCapturedPieces()) {
            board.removePiece(captured[0], captured[1]);
            log.info("Captured piece at [{}, {}]", captured[0], captured[1]);
        }
    }

    private boolean promoteIfKing(Board board, Move move) {
        Piece piece = board.getPiece(move.getToRow(), move.getToCol());

        if (piece == null || piece.isKing()) return false;

        boolean shouldPromote = false;

        if (piece.getColor() == RED && move.getToRow() == 7) {
            shouldPromote = true;
        } else if (piece.getColor() == PieceColor.BLACK && move.getToRow() == 0) {
            shouldPromote = true;
        }

        if (shouldPromote) {
            piece.setKing(true);
            log.debug("Promoted {} piece to king at [{}, {}]",
                    piece.getColor(), move.getToRow(), move.getToCol());
        }

        return shouldPromote;
    }

    private static boolean isDirectionNotAllowedForPiece(Piece piece, int[] dir) {
        // Kings can move in any direction
        if (piece.isKing()) return false;

        // For regular pieces:
        // - RED: forward means row increasing (dir[0] > 0)
        // - BLACK: forward means row decreasing (dir[0] < 0)
        if (piece.getColor() == PieceColor.RED) {
            return dir[0] <= 0; // red moves downwards (increase row)
        } else {
            return dir[0] >= 0; // black moves upwards (decrease row)
        }
    }

    private boolean isGameOver(Board board, PieceColor playerToMove, Game game) {
        boolean playerHasPieces = false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == playerToMove) {
                    playerHasPieces = true;
                    break;
                }
            }
            if (playerHasPieces) break;
        }

        // If player has no pieces
        if (!playerHasPieces) {
            log.info("Game over: {} has no pieces, opponent wins", playerToMove);
            AppUser winner = (playerToMove == PieceColor.RED) ? game.getBlackPlayer() : game.getRedPlayer();
            game.markFinished(winner, (playerToMove == PieceColor.RED) ? BLACK_WIN : RED_WIN);
            return true;
        }

        // If playerToMove has no valid moves
        List<Move> validMoves = getAvailableMoves(board, playerToMove);
        if (validMoves.isEmpty()) {
            log.info("Game over: {} has no valid moves, loses", playerToMove);
            AppUser winner = (playerToMove == PieceColor.RED) ? game.getBlackPlayer() : game.getRedPlayer();
            game.markFinished(winner, (playerToMove == PieceColor.RED) ? BLACK_WIN : RED_WIN);
            return true;
        }

        // Draw conditions
        if (game.getMovesWithoutCaptureOrPromotion() >= 80) {
            log.info("Game over: draw (80-move rule)");
            game.markFinished(null, GameResult.DRAW, "80 moves without capture/promotion");
            return true;
        }

        if (game.getTotalMoves() >= 200) {
            log.info("Game over: draw (200 total moves)");
            game.markFinished(null, GameResult.DRAW, "200 total moves reached");
            return true;
        }

        return false;
    }

    private Game findGameByIdWithPlayers(Long gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> {
                   log.info("Game not found with id {}.", gameId);
                   return new GameNotFoundException();
                });
    }
}