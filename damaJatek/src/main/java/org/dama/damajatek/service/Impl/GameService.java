package org.dama.damajatek.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.game.GameAlreadyFinishedException;
import org.dama.damajatek.exception.game.GameNotFoundException;
import org.dama.damajatek.exception.game.InvalidMoveException;
import org.dama.damajatek.mapper.GameMapper;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.repository.GameRepository;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.util.BoardInitializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.dama.damajatek.enums.game.GameStatus.FINISHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final ObjectMapper objectMapper;
    private final GameRepository gameRepository;
    private final IAppUserService appUserService;

    // The forced capture rule is used, so if there's a capture, then the user only gets that move.

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

        saveBoard(game, board);
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
                        ? PieceColor.RED
                        : PieceColor.BLACK;

        Board board = loadBoard(game);
        List<Move> validMoves = findAllValidMoves(board, game.getCurrentTurn());

        return CompletableFuture.completedFuture(
                GameMapper.createGameInfoDtoV1(game, board, validMoves, playerColor)
        );
    }

    @Transactional
    @Override
    public void makeMove(Long gameId, Move move) {
        Game game = findGameByIdWithPlayers(gameId);
        AppUser loggedInUser = appUserService.getLoggedInUser();

        checkUserAccessToGame(game, loggedInUser);

        if (game.getStatus() == FINISHED) {
            throw new GameAlreadyFinishedException();
        }

        Board board = loadBoard(game);
        PieceColor currentTurn = game.getCurrentTurn();

        if ((currentTurn == PieceColor.RED && !loggedInUser.getId().equals(game.getRedPlayer().getId())) ||
                (currentTurn == PieceColor.BLACK && !loggedInUser.getId().equals(game.getBlackPlayer().getId()))) {
            throw new AccessDeniedException("Not your turn");
        }

        // Check for forced captures
        List<Move> captureMoves = findAllCaptureMoves(board, currentTurn);
        List<Move> validMoves;

        if (!captureMoves.isEmpty()) {
            // If captures exist, only those are valid (forced capture rule)
            validMoves = captureMoves;
        } else {
            // Otherwise, all regular moves are valid
            validMoves = findAllValidMoves(board, currentTurn);
        }

        // Check if the move is on the list of valid moves
        if (!isMoveInValidMoves(validMoves, move)) {
            log.warn("Move not in valid moves list for game {}: {}", gameId, move);
            throw new InvalidMoveException("Invalid move - forced capture rule violation or move not available");
        }

        applyMove(board, move);
        promoteIfKing(board, move);

        // Switch turns
        PieceColor nextTurn = (currentTurn == PieceColor.RED)
                ? PieceColor.BLACK
                : PieceColor.RED;

        if (isGameOver(board, nextTurn)) {
            game.setStatus(FINISHED);
            game.setWinner(
                    game.getCurrentTurn() == PieceColor.RED ? game.getRedPlayer() : game.getBlackPlayer()
            );
            log.info("Game {} finished. Winner: {}", gameId, currentTurn);
        } else {
            game.setCurrentTurn(nextTurn);
        }

        saveBoard(game, board);

        log.info("Move executed in game {}", gameId);
        gameRepository.save(game);
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

    private boolean isMoveInValidMoves(List<Move> validMoves, Move move) {
        return validMoves.stream()
                .anyMatch(m -> m.getFromRow() == move.getFromRow() &&
                        m.getFromCol() == move.getFromCol() &&
                        m.getToRow() == move.getToRow() &&
                        m.getToCol() == move.getToCol());
    }






    // Iterates through the full table, searches for the pieces with the given color.
    // It uses the findValidMovesForPiece method, to find all the possible moves for all the pieces.
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
            if (!piece.isKing()) {
                if (piece.getColor() == PieceColor.RED && dir[0] < 0) continue;
                if (piece.getColor() == PieceColor.BLACK && dir[0] > 0) continue;
            }

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
        List<Move> captures = new ArrayList<>();
        Piece piece = board.getPiece(row, col);

        if (piece == null) return captures;

        // Check all four diagonal directions
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            // Skip backward moves for regular pieces
            if (!piece.isKing()) {
                if (piece.getColor() == PieceColor.RED && dir[0] < 0) continue;
                if (piece.getColor() == PieceColor.BLACK && dir[0] > 0) continue;
            }

            int jumpRow = row + dir[0] * 2;
            int jumpCol = col + dir[1] * 2;
            int middleRow = row + dir[0];
            int middleCol = col + dir[1];

            if (isInBounds(jumpRow, jumpCol) &&
                    board.getPiece(jumpRow, jumpCol) == null) {

                Piece middle = board.getPiece(middleRow, middleCol);
                if (middle != null && middle.getColor() != piece.getColor()) {
                    Move capture = Move.builder()
                            .fromRow(row)
                            .fromCol(col)
                            .toRow(jumpRow)
                            .toCol(jumpCol)
                            .build();
                    capture.getCapturedPieces().add(new int[]{middleRow, middleCol});
                    captures.add(capture);
                }
            }
        }

        return captures;
    }






    // Checks if the given row&col is on the board.
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
            log.debug("Captured piece at [{}, {}]", captured[0], captured[1]);
        }
    }

    private void promoteIfKing(Board board, Move move) {
        Piece piece = board.getPiece(move.getToRow(), move.getToCol());

        if (piece == null || piece.isKing()) return;

        boolean shouldPromote = false;

        if (piece.getColor() == PieceColor.RED && move.getToRow() == 7) {
            shouldPromote = true;
        } else if (piece.getColor() == PieceColor.BLACK && move.getToRow() == 0) {
            shouldPromote = true;
        }

        if (shouldPromote) {
            piece.setKing(true);
            log.debug("Promoted {} piece to king at [{}, {}]",
                    piece.getColor(), move.getToRow(), move.getToCol());
        }
    }

    private boolean isGameOver(Board board, PieceColor currentTurn) {
        // Check if current player has any pieces left
        boolean hasPieces = false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == currentTurn) {
                    hasPieces = true;
                    break;
                }
            }
            if (hasPieces) break;
        }

        if (!hasPieces) {
            log.info("Game over: {} has no pieces left", currentTurn);
            return true;
        }

        // Check if current player has any valid moves
        List<Move> validMoves = findAllValidMoves(board, currentTurn);
        if (validMoves.isEmpty()) {
            log.info("Game over: {} has no valid moves", currentTurn);
            return true;
        }

        return false;
    }

    private Board loadBoard(Game game) {
        try {
            return objectMapper.readValue(game.getBoardState(), Board.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to load board for game {}: {}", game.getId(), e.getMessage());
            throw new RuntimeException("Failed to load board", e);
        }
    }

    private void saveBoard(Game game, Board board) {
        try {
            game.setBoardState(objectMapper.writeValueAsString(board));
        } catch (JsonProcessingException e) {
            log.error("Failed to save board for game {}: {}", game.getId(), e.getMessage());
            throw new RuntimeException("Failed to save board", e);
        }
    }

    private Game findGameByIdWithPlayers(Long gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> {
                   log.info("Game not found with id {}.", gameId);
                   return new GameNotFoundException();
                });
    }
}