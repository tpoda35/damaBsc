package org.dama.damajatek.bot;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.service.IGameEngine;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.dama.damajatek.enums.game.PieceColor.RED;
import static org.dama.damajatek.util.BoardSerializer.copy;
import static org.dama.damajatek.util.BotUtils.boardHash;
import static org.dama.damajatek.util.BotUtils.opposite;

@RequiredArgsConstructor
public class MediumBot implements IBotStrategy {

    private final IGameEngine gameEngine;
    private static final int MAX_DEPTH = 3;

    private final Queue<Integer> recentStates = new LinkedList<>();
    private static final int MAX_HISTORY = 6;

    @Override
    public Move chooseMove(Board board, PieceColor color) {
        List<Move> moves = gameEngine.getAvailableMoves(board, color);
        if (moves.isEmpty()) return null;

        if (moves.size() == 1) {
            recordBoardState(board, moves.getFirst());
            return moves.getFirst();
        }

        // Sort moves to prioritize better moves first
        moves.sort((m1, m2) -> Integer.compare(
                evaluateMoveQuality(board, m2),
                evaluateMoveQuality(board, m1)
        ));

        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        for (Move move : moves) {
            Board simulated = copy(board);
            gameEngine.applyMove(simulated, move);
            gameEngine.promoteIfKing(simulated, move);

            int value = minimax(simulated, opposite(color), 1, MAX_DEPTH,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, color);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        if (bestMove != null) recordBoardState(board, bestMove);

        return bestMove != null ? bestMove : moves.getFirst();
    }

    private int evaluateMoveQuality(Board board, Move move) {
        int score = 0;

        // Evaluate captures if there's any
        if (!move.getCapturedPieces().isEmpty()) {
            for (int[] captured : move.getCapturedPieces()) {
                Piece capturedPiece = board.getPiece(captured[0], captured[1]);
                if (capturedPiece != null) score += capturedPiece.isKing() ? 50 : 20;
            }
        }

        // Promotion bonus
        if (move.getToRow() == 0 || move.getToRow() == 7) score += 25;

        // Center control
        int centerDistance = Math.abs(move.getToRow() - 3) + Math.abs(move.getToCol() - 3);
        score += (6 - centerDistance); // Closer to center = higher score

        // Forward advancement (non-king pieces)
        Piece movingPiece = board.getPiece(move.getFromRow(), move.getFromCol());
        if (movingPiece != null && !movingPiece.isKing()) {
            int advancement = (movingPiece.getColor() == RED)
                    ? (move.getToRow() - move.getFromRow())
                    : (move.getFromRow() - move.getToRow());
            if (advancement > 0) score += advancement * 3;
        }

        // Prefer moves that keep pieces protected (on back rows or edges initially)
        if (move.getToRow() == 0 || move.getToRow() == 7 ||
                move.getToCol() == 0 || move.getToCol() == 7) {
            score += 2;
        }

        return score;
    }

    private void recordBoardState(Board board, Move move) {
        Board next = copy(board);
        gameEngine.applyMove(next, move);
        gameEngine.promoteIfKing(next, move);

        int hash = boardHash(next);
        recentStates.add(hash);

        if (recentStates.size() > MAX_HISTORY) {
            recentStates.poll();
        }
    }

    private int minimax(Board board, PieceColor turn, int depth, int maxDepth,
                        int alpha, int beta, PieceColor maximizingColor) {

        List<Move> moves = gameEngine.getAvailableMoves(board, turn);

        if (depth >= maxDepth || moves.isEmpty()) {
            return evaluateBoard(board, maximizingColor);
        }

        if (turn == maximizingColor) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
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

    private int evaluateBoard(Board board, PieceColor color) {
        int redScore = 0;
        int whiteScore = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                var piece = board.getPiece(r, c);
                if (piece == null) continue;

                int value = (piece.isKing()) ? 5 : 2;

                // Bonus for center control
                if (r >= 2 && r <= 5 && c >= 2 && c <= 5) {
                    value += 1;
                }

                // Bonus for approaching promotion
                if (!piece.isKing()) {
                    value += (int) ((piece.getColor() == RED) ? (7 - r) * 0.5 : r * 0.5);
                }

                if (piece.getColor() == RED) redScore += value;
                else whiteScore += value;
            }
        }

        int value = (color == RED)
                ? (redScore - whiteScore)
                : (whiteScore - redScore);

        // Penalize repetition to avoid loops
        int hash = boardHash(board);
        int penalty = 0;
        int distance = 0;

        for (int oldHash : recentStates) {
            if (oldHash == hash) {
                // Most recent repetition gets highest penalty
                penalty = Math.max(1, MAX_HISTORY - distance);
                break;
            }
            distance++;
        }

        value -= penalty;

        return value;
    }
}