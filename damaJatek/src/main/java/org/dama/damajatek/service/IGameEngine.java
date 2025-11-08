package org.dama.damajatek.service;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

import java.util.List;

public interface IGameEngine {
    List<Move> getAvailableMoves(Board board, PieceColor color);
    void applyMove(Board board, Move move);
    boolean promoteIfKing(Board board, Move move);
    boolean isGameOver(Board board, PieceColor playerToMove, Game game);
}
