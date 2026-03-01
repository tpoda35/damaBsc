package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.service.IGameEngine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dama.damajatek.enums.game.GameResult.*;
import static org.dama.damajatek.enums.game.PieceColor.RED;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameEngine implements IGameEngine {

    // Chains the findAllCaptureMoves and findAllValidMoves.
    // If there's any capture, then only that returns
    // If there's no capture, then the other valid moves returns
    @Override
    public List<Move> getAvailableMoves(Board board, PieceColor color) {
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

    @Override
    public void applyMove(Board board, Move move) {
        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());
        board.setPiece(move.getToRow(), move.getToCol(), piece);
        board.removePiece(move.getFromRow(), move.getFromCol());

        // Remove captured pieces
        for (int[] captured : move.getCapturedPieces()) {
            board.removePiece(captured[0], captured[1]);
        }
    }

    @Override
    public boolean promoteIfKing(Board board, Move move) {
        Piece piece = board.getPiece(move.getToRow(), move.getToCol());

        if (piece == null || piece.isKing()) return false;

        boolean shouldPromote = false;

        if (piece.getColor() == RED && move.getToRow() == 7) {
            shouldPromote = true;
        } else if (piece.getColor() == PieceColor.WHITE && move.getToRow() == 0) {
            shouldPromote = true;
        }

        if (shouldPromote) {
            piece.setKing(true);
        }

        return shouldPromote;
    }

    private static boolean isDirectionNotAllowedForPiece(Piece piece, int[] dir) {
        // Kings can move in any direction
        if (piece.isKing()) return false;

        // For regular pieces:
        // - RED: forward means row increasing (dir[0] > 0)
        // - WHITE: forward means row decreasing (dir[0] < 0)
        if (piece.getColor() == RED) {
            return dir[0] <= 0; // red moves downwards (increase row)
        } else {
            return dir[0] >= 0; // white moves upwards (decrease row)
        }
    }

    @Override
    public boolean isGameOver(Board board, PieceColor playerToMove, Game game) {
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
            Player winner = (playerToMove == RED) ? game.getWhitePlayer() : game.getRedPlayer();
            game.markFinished(winner, (playerToMove == RED) ? WHITE_WIN : RED_WIN);
            return true;
        }

        // If playerToMove has no valid moves
        List<Move> validMoves = getAvailableMoves(board, playerToMove);
        if (validMoves.isEmpty()) {
            log.info("Game over: {} has no valid moves, loses", playerToMove);
            Player winner = (playerToMove == RED) ? game.getWhitePlayer() : game.getRedPlayer();
            game.markFinished(winner, (playerToMove == RED) ? WHITE_WIN : RED_WIN);
            return true;
        }

        // Draw conditions
        if (game.getMovesWithoutCaptureOrPromotion() >= 80) {
            log.info("Game over: draw (80-move rule)");
            game.markFinished(null, DRAW, "80 moves without capture/promotion");
            return true;
        }

        if (game.getTotalMoves() >= 200) {
            log.info("Game over: draw (200 total moves)");
            game.markFinished(null, DRAW, "200 total moves reached");
            return true;
        }

        return false;
    }
}
