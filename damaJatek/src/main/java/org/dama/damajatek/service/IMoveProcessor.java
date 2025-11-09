package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.MoveResult;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

public interface IMoveProcessor {
    MoveResult processMove(Game game, Board board, Move move);
}
