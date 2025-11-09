package org.dama.damajatek.bot;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.service.IGameEngine;
import org.dama.damajatek.util.BoardSerializer;

import java.util.List;

@RequiredArgsConstructor
public class MediumBot implements IBotStrategy {

    private final IGameEngine gameEngine;
    private static final int MAX_DEPTH = 4;

    @Override
    public Move chooseMove(Board board, PieceColor color) {
        List<Move> moves = gameEngine.getAvailableMoves(board, color);
        if (moves.isEmpty()) return null;

        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        for (Move move : moves) {
            Board simulated = BoardSerializer.copy(board);
            gameEngine.applyMove(simulated, move);
            gameEngine.promoteIfKing(simulated, move);

            int value = minimax(simulated, opposite(color), 1, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, color);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove != null ? bestMove : moves.getFirst();
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
                Board copy = BoardSerializer.copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // pruning
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board copy = BoardSerializer.copy(board);
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
                if (piece.getColor() == PieceColor.RED) redScore += value;
                else blackScore += value;
            }
        }

        return (color == PieceColor.RED)
                ? (redScore - blackScore)
                : (blackScore - redScore);
    }
}

