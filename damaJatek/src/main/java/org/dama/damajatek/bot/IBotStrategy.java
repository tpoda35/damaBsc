package org.dama.damajatek.bot;

import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

public interface IBotStrategy {
    Move chooseMove(Board board, PieceColor pieceColor);
}
