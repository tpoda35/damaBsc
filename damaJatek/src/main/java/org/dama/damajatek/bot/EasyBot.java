package org.dama.damajatek.bot;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.service.IGameEngine;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static org.dama.damajatek.util.BoardSerializer.copy;
import static org.dama.damajatek.util.BotUtils.boardHash;
import static org.dama.damajatek.util.BotUtils.opposite;

@RequiredArgsConstructor
public class EasyBot implements IBotStrategy {

    private final IGameEngine gameEngine;
    private static final int MAX_DEPTH = 1;
    private static final double BLUNDER_CHANCE = 0.15; // 15% chance to make a random move

    private final Queue<Integer> recentStates = new LinkedList<>();
    private static final int MAX_HISTORY = 6;
    private final Random random = new Random();

    @Override
    public Move chooseMove(Board board, PieceColor color) {
        List<Move> moves = gameEngine.getAvailableMoves(board, color);
        if (moves.isEmpty()) return null;

        if (moves.size() == 1) {
            recordBoardState(board, moves.getFirst());
            return moves.getFirst();
        }

        // Occasionally make a blunder
        if (random.nextDouble() < BLUNDER_CHANCE) {
            Move blunderMove = moves.get(random.nextInt(moves.size()));
            recordBoardState(board, blunderMove);
            return blunderMove;
        }

        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        for (Move move : moves) {
            Board simulated = copy(board);
            gameEngine.applyMove(simulated, move);
            gameEngine.promoteIfKing(simulated, move);

            int value = minimax(simulated, opposite(color), 1, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, color);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        if (bestMove != null) {
            recordBoardState(board, bestMove);
        }

        return bestMove != null ? bestMove : moves.getFirst();
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

        if (depth >= maxDepth || gameEngine.getAvailableMoves(board, turn).isEmpty()) {
            return evaluateBoard(board, maximizingColor);
        }

        List<Move> moves = gameEngine.getAvailableMoves(board, turn);

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

                int value = piece.isKing() ? 3 : 1;

                if (piece.getColor() == PieceColor.RED) redScore += value;
                else whiteScore += value;
            }
        }

        int value = (color == PieceColor.RED)
                ? (redScore - whiteScore)
                : (whiteScore - redScore);

        // Penalize repetition to avoid loops
        int hash = boardHash(board);
        int penalty = 0;
        int distance = 0;

        for (int oldHash : recentStates) {
            if (oldHash == hash) {
                // Most recent repetition gets highest penalty
                penalty = Math.max(2, (MAX_HISTORY - distance) * 2);
                break;
            }
            distance++;
        }

        value -= penalty;

        return value;
    }
}