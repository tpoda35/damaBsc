package org.dama.damajatek.bot;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.dto.game.TranspositionEntry;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.service.IGameEngine;

import java.util.*;

import static org.dama.damajatek.util.BoardSerializer.copy;
import static org.dama.damajatek.util.BotUtils.boardHash;
import static org.dama.damajatek.util.BotUtils.opposite;

@RequiredArgsConstructor
public class HardBot implements IBotStrategy {

    private final IGameEngine gameEngine;
    private static final int MAX_DEPTH = 9; // Maximum depth for minimax search, iterative deep. starts from 1 and climbs to this value
    private static final int MAX_TRANSPOSITION_SIZE = 100000; // Cache size limit

    // Table for storing board hash (eval + depth)
    // Allows reusing computed minimax values from earlier iterations
    private final Map<Integer, TranspositionEntry> transposition = new HashMap<>();

    private final Queue<Integer> recentStates = new LinkedList<>(); // For repetition check
    private static final int MAX_HISTORY = 6; // Of recentStates

    @Override
    public Move chooseMove(Board board, PieceColor color) {
        List<Move> moves = gameEngine.getAvailableMoves(board, color);
        if (moves.isEmpty()) return null;

        // If only one move exists, no need to search
        if (moves.size() == 1) {
            recordBoardState(board, moves.getFirst());
            return moves.getFirst();
        }

        // Clear transposition table if it gets too large
        if (transposition.size() > MAX_TRANSPOSITION_SIZE) transposition.clear();

        // Compute best move using iterative deepening
        Move bestMove = iterativeDeepening(board, color, moves);

        // Record the resulting board state to avoid repetition
        if (bestMove != null) recordBoardState(board, bestMove);

        return bestMove != null ? bestMove : moves.getFirst();
    }

    private Move iterativeDeepening(Board board, PieceColor color, List<Move> moves) {
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        // Sort initial moves by heuristic
        // This improves pruning
        moves.sort((m1, m2) -> Integer.compare(
                evaluateMoveQuality(board, m2),
                evaluateMoveQuality(board, m1)
        ));

        // Each iteration uses results from the previous iteration to reorder moves
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            int currentBest = Integer.MIN_VALUE;
            Move currentBestMove = null;

            for (Move move : moves) {
                Board simulated = copy(board);
                gameEngine.applyMove(simulated, move);
                gameEngine.promoteIfKing(simulated, move);

                int value = minimax(simulated, opposite(color), 1, depth,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, color);

                if (value > currentBest) {
                    currentBest = value;
                    currentBestMove = move;
                }
            }

            // Update best move if we found a better one
            if (currentBest > bestValue || bestMove == null) {
                bestValue = currentBest;
                bestMove = currentBestMove;
            }

            // Move best move to front so next iteration searches it first
            // This improves pruning
            if (currentBestMove != null) {
                moves.remove(currentBestMove);
                moves.addFirst(currentBestMove);
            }
        }

        return bestMove;
    }

    private int evaluateMoveQuality(Board board, Move move) {
        int score = 0;

        // Evaluate captures if there's any
        if (!move.getCapturedPieces().isEmpty()) {
            for (int[] captured : move.getCapturedPieces()) {
                Piece capturedPiece = board.getPiece(captured[0], captured[1]);
                if (capturedPiece != null) score += capturedPiece.isKing() ? 50 : 20; // King captures worth significantly more
            }
        }

        // Promotion bonus
        if (move.getToRow() == 0 || move.getToRow() == 7) {
            Piece movingPiece = board.getPiece(move.getFromRow(), move.getFromCol());
            if (movingPiece != null && !movingPiece.isKing()) score += 30;
        }

        // Center control
        double centerDist = Math.abs(move.getToRow() - 3.5) + Math.abs(move.getToCol() - 3.5);
        score += (int)((7 - centerDist) * 2);

        // Forward advancement (non-king pieces)
        Piece movingPiece = board.getPiece(move.getFromRow(), move.getFromCol());
        if (movingPiece != null && !movingPiece.isKing()) {
            int advancement = (movingPiece.getColor() == PieceColor.RED)
                    ? (move.getFromRow() - move.getToRow())
                    : (move.getToRow() - move.getFromRow());
            if (advancement > 0) score += advancement * 5;
        }

        return score;
    }

    private void recordBoardState(Board board, Move move) {
        Board next = copy(board);
        gameEngine.applyMove(next, move);
        gameEngine.promoteIfKing(next, move);

        int hash = boardHash(next);
        recentStates.add(hash);

        if (recentStates.size() > MAX_HISTORY) recentStates.poll();
    }

    private int minimax(Board board, PieceColor turn, int depth, int maxDepth,
                        int alpha, int beta, PieceColor maximizingColor) {

        int hash = boardHash(board);

        // Check transposition table
        // Only use if from equal or deeper search
        TranspositionEntry entry = transposition.get(hash);
        if (entry != null && entry.getDepth() >= (maxDepth - depth)) return entry.getValue();

        List<Move> moves = gameEngine.getAvailableMoves(board, turn);

        // Terminal node
        if (depth >= maxDepth || moves.isEmpty()) {
            int eval = evaluateBoard(board, maximizingColor);
            transposition.put(hash, new TranspositionEntry(eval, maxDepth - depth));
            return eval;
        }

        // Sort moves for better pruning
        moves.sort((m1, m2) -> Integer.compare(
                evaluateMoveQuality(board, m2),
                evaluateMoveQuality(board, m1)
        ));

        int result;
        if (turn == maximizingColor) {
            // Max player
            result = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                result = Math.max(result, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha) break; // Prune
            }
        } else {
            // Min player
            result = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                result = Math.min(result, eval);
                beta = Math.min(beta, eval);

                if (beta <= alpha) break; // Prune
            }
        }

        // Save result in transposition table
        transposition.put(hash, new TranspositionEntry(result, maxDepth - depth));
        return result;
    }

    private int evaluateBoard(Board board, PieceColor color) {
        int red = 0, black = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                var p = board.getPiece(r, c);
                if (p == null) continue;

                int value = p.isKing() ? 5 : 2;

                // Bonus for center control
                if (r >= 2 && r <= 5 && c >= 2 && c <= 5)
                    value += 2;

                // Bonus for approaching promotion
                if (!p.isKing())
                    value += (p.getColor() == PieceColor.RED ? (7 - r) : r);

                // Penalize threatened pieces
                if (isThreatened(board, r, c, p.getColor()))
                    value -= (p.isKing() ? 4 : 2);

                if (p.getColor() == PieceColor.RED) red += value;
                else black += value;
            }
        }

        // Add mobility (number of moves available)
        red += gameEngine.getAvailableMoves(board, PieceColor.RED).size();
        black += gameEngine.getAvailableMoves(board, PieceColor.BLACK).size();

        // Penalize repetition to avoid loops
        int hash = boardHash(board);
        int penalty = 0;
        int distance = 0;

        for (int h : recentStates) {
            if (h == hash) {
                // Most recent repetition gets highest penalty
                penalty = Math.max(1, MAX_HISTORY - distance);
                break;
            }
            distance++;
        }

        return (color == PieceColor.RED ? red - black : black - red) - penalty;
    }

    // Checks if any enemy move contains this square as a captured piece
    private boolean isThreatened(Board board, int r, int c, PieceColor color) {
        PieceColor enemy = opposite(color);
        List<Move> enemyMoves = gameEngine.getAvailableMoves(board, enemy);

        for (Move m : enemyMoves) {
            for (int[] cp : m.getCapturedPieces()) {
                if (cp[0] == r && cp[1] == c) return true;
            }
        }
        return false;
    }
}