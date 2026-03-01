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
    private static final int MAX_DEPTH = 6;
    private static final int ENDGAME_DEPTH = 8;

    private final Map<Integer, TranspositionEntry> transpositionTable = new HashMap<>();
    private final Queue<Integer> recentStates = new LinkedList<>();
    private static final int MAX_HISTORY = 10;

    @Override
    public Move chooseMove(Board board, PieceColor color) {
        List<Move> moves = gameEngine.getAvailableMoves(board, color);
        if (moves.isEmpty()) return null;

        if (moves.size() == 1) {
            recordBoardState(board, moves.getFirst());
            return moves.getFirst();
        }

        int searchDepth = getAdaptiveDepth(board);

        moves = orderMoves(board, moves);

        Move bestMove = null;

        for (int depth = 1; depth <= searchDepth; depth++) {
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

            if (currentBestMove != null) {
                bestMove = currentBestMove;
            }
        }

        if (bestMove != null) recordBoardState(board, bestMove);

        return bestMove != null ? bestMove : moves.getFirst();
    }

    private int getAdaptiveDepth(Board board) {
        int pieceCount = countTotalPieces(board);

        // Endgame: fewer pieces, search deeper
        if (pieceCount <= 6) return ENDGAME_DEPTH;
        if (pieceCount <= 10) return MAX_DEPTH + 1;
        return MAX_DEPTH;
    }

    private int countTotalPieces(Board board) {
        int count = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board.getPiece(r, c) != null) count++;
            }
        }
        return count;
    }

    private List<Move> orderMoves(Board board, List<Move> moves) {
        // Score each move for ordering
        List<ScoredMove> scoredMoves = new ArrayList<>();

        for (Move move : moves) {
            int score = evaluateMoveQuality(board, move);
            scoredMoves.add(new ScoredMove(move, score));
        }

        // Sort in descending order (best moves first)
        scoredMoves.sort((m1, m2) -> Integer.compare(m2.score, m1.score));

        List<Move> orderedMoves = new ArrayList<>();
        for (ScoredMove sm : scoredMoves) {
            orderedMoves.add(sm.move);
        }
        return orderedMoves;
    }

    private static class ScoredMove {
        Move move;
        int score;

        ScoredMove(Move move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    private int evaluateMoveQuality(Board board, Move move) {
        int score = 0;

        // Captures are highest priority
        if (!move.getCapturedPieces().isEmpty()) {
            for (int[] captured : move.getCapturedPieces()) {
                Piece capturedPiece = board.getPiece(captured[0], captured[1]);
                if (capturedPiece != null) {
                    score += capturedPiece.isKing() ? 100 : 50;
                }
            }
        }

        // King promotion
        if (move.getToRow() == 0 || move.getToRow() == 7) {
            score += 40;
        }

        // Strategic positioning
        Piece movingPiece = board.getPiece(move.getFromRow(), move.getFromCol());
        if (movingPiece != null) {
            // Center control (more valuable for kings)
            int centerDistance = Math.abs(move.getToRow() - 3) + Math.abs(move.getToCol() - 3);
            score += movingPiece.isKing() ? (8 - centerDistance) : (6 - centerDistance);

            // Forward progression for non-kings
            if (!movingPiece.isKing()) {
                int advancement = (movingPiece.getColor() == PieceColor.RED)
                        ? (move.getToRow() - move.getFromRow())
                        : (move.getFromRow() - move.getToRow());
                score += advancement * 5;
            }

            // Defensive positioning (back row protection for non-kings)
            if (!movingPiece.isKing() &&
                    (move.getToRow() == 0 || move.getToRow() == 7)) {
                score += 3;
            }
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

        int boardHash = boardHash(board);

        // Check transposition table
        TranspositionEntry entry = transpositionTable.get(boardHash);
        if (entry != null && entry.getDepth() >= (maxDepth - depth)) {
            if (entry.getFlag() == 0) return entry.getValue(); // Exact value
            if (entry.getFlag() == 1) alpha = Math.max(alpha, entry.getValue()); // Lower bound
            if (entry.getFlag() == 2) beta = Math.min(beta, entry.getValue()); // Upper bound
            if (alpha >= beta) return entry.getValue();
        }

        List<Move> moves = gameEngine.getAvailableMoves(board, turn);

        if (depth >= maxDepth || moves.isEmpty()) {
            int eval = evaluateBoard(board, maximizingColor);
            transpositionTable.put(boardHash, new TranspositionEntry(eval, maxDepth - depth, 0));
            return eval;
        }

        // Move ordering for better pruning
        moves = orderMoves(board, moves);

        if (turn == maximizingColor) {
            int maxEval = Integer.MIN_VALUE;
            int originalAlpha = alpha;

            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Beta cutoff
            }

            // Store in transposition table
            int flag = maxEval <= originalAlpha ? 2 : (maxEval >= beta ? 1 : 0);
            transpositionTable.put(boardHash, new TranspositionEntry(maxEval, maxDepth - depth, flag));

            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            int originalBeta = beta;

            for (Move move : moves) {
                Board copy = copy(board);
                gameEngine.applyMove(copy, move);
                gameEngine.promoteIfKing(copy, move);

                int eval = minimax(copy, opposite(turn), depth + 1, maxDepth, alpha, beta, maximizingColor);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha cutoff
            }

            // Store in transposition table
            int flag = minEval >= originalBeta ? 1 : (minEval <= alpha ? 2 : 0);
            transpositionTable.put(boardHash, new TranspositionEntry(minEval, maxDepth - depth, flag));

            return minEval;
        }
    }

    private int evaluateBoard(Board board, PieceColor color) {
        int redScore = 0;
        int whiteScore = 0;

        int redPieces = 0, whitePieces = 0;
        int redKings = 0, blackKings = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece == null) continue;

                int value = piece.isKing() ? 10 : 3;

                // Positional bonuses
                // Center control (stronger for kings)
                if (r >= 2 && r <= 5 && c >= 2 && c <= 5) {
                    value += piece.isKing() ? 2 : 1;
                }

                // Advancement bonus for non-kings
                if (!piece.isKing()) {
                    int advancement = (piece.getColor() == PieceColor.RED) ? (7 - r) : r;
                    value += advancement / 2;
                }

                // Edge protection for non-kings in defensive positions
                if (!piece.isKing() && (c == 0 || c == 7)) {
                    value += 1;
                }

                // Back row bonus (defensive strength)
                if ((piece.getColor() == PieceColor.RED && r == 7) ||
                        (piece.getColor() == PieceColor.WHITE && r == 0)) {
                    value += 2;
                }

                if (piece.getColor() == PieceColor.RED) {
                    redScore += value;
                    redPieces++;
                    if (piece.isKing()) redKings++;
                } else {
                    whiteScore += value;
                    whitePieces++;
                    if (piece.isKing()) blackKings++;
                }
            }
        }

        // Material advantage bonus
        int myPieces = (color == PieceColor.RED) ? redPieces : whitePieces;
        int oppPieces = (color == PieceColor.RED) ? whitePieces : redPieces;
        int materialAdvantage = (myPieces - oppPieces) * 5;

        // King advantage in endgame
        int myKings = (color == PieceColor.RED) ? redKings : blackKings;
        int oppKings = (color == PieceColor.RED) ? blackKings : redKings;
        int kingAdvantage = (myKings - oppKings) * 8;

        int baseValue = (color == PieceColor.RED)
                ? (redScore - whiteScore)
                : (whiteScore - redScore);

        int totalValue = baseValue + materialAdvantage + kingAdvantage;

        // Repetition penalty
        int hash = boardHash(board);
        int penalty = 0;
        int distance = 0;

        for (int oldHash : recentStates) {
            if (oldHash == hash) {
                penalty = (MAX_HISTORY - distance) * 2;
                break;
            }
            distance++;
        }

        totalValue -= penalty;

        return totalValue;
    }
}