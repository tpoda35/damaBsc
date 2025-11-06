package org.dama.damajatek.bot;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.service.IGameEngine;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class EasyBot implements IBotStrategy {

    private final IGameEngine gameEngine;
    private final Random random = new Random();

    @Override
    public Move chooseMove(Board board, PieceColor pieceColor) {
        List<Move> validMoves = gameEngine.getAvailableMoves(board, pieceColor);
        if (validMoves.isEmpty()) return null;
        return validMoves.get(random.nextInt(validMoves.size()));
    }

}
