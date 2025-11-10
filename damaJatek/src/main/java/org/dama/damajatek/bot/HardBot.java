package org.dama.damajatek.bot;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.service.IGameEngine;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.dama.damajatek.util.BoardSerializer.copy;

@RequiredArgsConstructor
public class HardBot implements IBotStrategy {

    private final IGameEngine gameEngine;
    private static final int MAX_DEPTH = 9;

    private final Queue<Integer> recentStates = new LinkedList<>();
    private static final int MAX_HISTORY = 6;

    @Override
    public Move chooseMove(Board board, PieceColor color) {
        List<Move> moves = gameEngine.getAvailableMoves(board, color);
        if (moves.isEmpty()) return null;

        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        for (Move move : moves) {
            Board simulated = copy(board);
            gameEngine.applyMove(simulated, move);
            gameEngine.promoteIfKing(simulated, move);

            // Start minimax, with depth 1, since we already did the dept 0
            int value = minimax(simulated, opposite(color), 1, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, color);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        if (bestMove != null) {
            // Simulate the board after applying the chosen move
            Board next = copy(board);
            gameEngine.applyMove(next, bestMove);
            gameEngine.promoteIfKing(next, bestMove);

            // Record new board state
            int hash = boardHash(next);
            recentStates.add(hash);

            // Keep only the most recent few board states
            if (recentStates.size() > MAX_HISTORY) {
                recentStates.poll(); // Remove oldest entry
            }
        }

        return bestMove != null ? bestMove : moves.getFirst();
    }

    // It starts from the bottom of the recursive chain
    private int minimax(Board board, PieceColor turn, int depth, int maxDepth,
                        int alpha, int beta, PieceColor maximizingColor) {

        if (depth >= maxDepth || gameEngine.getAvailableMoves(board, turn).isEmpty()) {
            return evaluateBoard(board, maximizingColor);
        }

        List<Move> moves = gameEngine.getAvailableMoves(board, turn);

        // If it's the maximizing players turn, try to maximize the eval
        // If it's the other players turn, then it minimizes it
        if (turn == maximizingColor) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                // pruning, if we found a move which is worse than a previous one, then stop
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private PieceColor opposite(PieceColor c) {
        return (c == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
    }

    private int evaluateBoard(Board board, PieceColor color) {
        int redScore = 0;
        int blackScore = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                var piece = board.getPiece(r, c);
                if (piece == null) continue;

                int value = (piece.isKing()) ? 3 : 1;

                // Encourage central positions
                if (r >= 2 && r <= 5 && c >= 2 && c <= 5) {
                    value += 1;
                }

                // Encourage advancing towards becoming a king
                if (!piece.isKing()) {
                    value += (int) ((piece.getColor() == PieceColor.RED) ? (7 - r) * 0.2 : r * 0.2);
                }

                if (piece.getColor() == PieceColor.RED) redScore += value;
                else blackScore += value;
            }
        }

        int value = (color == PieceColor.RED)
                ? (redScore - blackScore)
                : (blackScore - redScore);

        // This is for to avoid repeats
        int hash = boardHash(board);
        int penalty = 0;
        int distance = 0;

        for (int oldHash : recentStates) {
            if (oldHash == hash) {
                // Closer repeats = stronger penalty the bot gets (5 -> 1)
                penalty = Math.max(1, 5 - distance);
                break;
            }
            distance++;
        }

        value -= penalty; // apply penalty (0 if no match)


        return value;
    }

    // Creates a hash from the board, to track the history and prevent repetition
    private int boardHash(Board board) {
        int hash = 7;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                var piece = board.getPiece(r, c);
                if (piece != null) {
                    int pieceVal = piece.getColor().ordinal() * 10 + (piece.isKing() ? 2 : 1);
                    hash = 31 * hash + pieceVal + r * 8 + c;
                }
            }
        }
        return hash;
    }
}
