package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.dto.game.MoveInfoDtoV1;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

import java.util.List;

public class GameMapper {

    public static GameInfoDtoV1 createGameInfoDtoV1(Game game, Board board, List<Move> allowedMoves, PieceColor playerColor) {
        return GameInfoDtoV1.builder()
                .id(game.getId())
                .board(board)
                .currentTurn(game.getCurrentTurn())
                .allowedMoves(allowedMoves)
                .playerColor(playerColor)
                .build();
    }

    public static MoveInfoDtoV1 createGameInfoDtoV2(Game game, List<Move> allowedMoves) {
        return MoveInfoDtoV1.builder()
                .currentTurn(game.getCurrentTurn())
                .allowedMoves(allowedMoves)
                .build();
    }

}
