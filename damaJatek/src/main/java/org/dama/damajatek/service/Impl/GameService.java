package org.dama.damajatek.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.enums.game.GameStatus;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.repository.GameRepository;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.util.BoardInitializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final ObjectMapper objectMapper;
    private final GameRepository gameRepository;

    @Override
    @Transactional
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

    @Override
    @Transactional
    public Game makeMove(Long gameId, Move move) {
        return null;
    }

    private boolean isValidMove(Board board, Move move, PieceColor currentTurn) {
        // Bounds checking
        if (!isInBounds(move.getFromRow(), move.getFromCol()) ||
                !isInBounds(move.getToRow(), move.getToCol())) {
            return false;
        }

        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());
        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }

        // Destination must be empty
        if (board.getPiece(move.getToRow(), move.getToCol()) != null) {
            return false;
        }

        int rowDiff = move.getToRow() - move.getFromRow();
        int colDiff = move.getToCol() - move.getFromCol();

        // Must move diagonally
        if (Math.abs(rowDiff) != Math.abs(colDiff)) {
            return false;
        }

        // Direction validation for regular pieces
        if (!piece.isKing()) {
            if (piece.getColor() == PieceColor.RED && rowDiff < 0) {
                return false; // Red pieces can't move backward
            }
            if (piece.getColor() == PieceColor.BLACK && rowDiff > 0) {
                return false; // Black pieces can't move backward
            }
        }

        // Simple move (distance 1)
        if (Math.abs(rowDiff) == 1) {
            return true;
        }

        // Capture move (distance 2)
        if (Math.abs(rowDiff) == 2) {
            return isValidCapture(board, move, piece);
        }

        // Invalid distance
        return false;
    }

    private boolean isValidCapture(Board board, Move move, Piece movingPiece) {
        return false;
    }

    private boolean isCapture(Move move) {
        return Math.abs(move.getToRow() - move.getFromRow()) == 2;
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

        // First check captures
        moves.addAll(findCaptureMovesForPiece(board, row, col));

        // If captures exist, only return captures (forced capture rule)
        if (!moves.isEmpty()) return moves;

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

    // Checks if the piece is on the board.
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
}