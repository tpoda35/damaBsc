package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.websocket.MoveWsDto;
import org.dama.damajatek.model.Move;

public class MoveMapper {

    public static Move createMove(MoveWsDto moveWsDto) {
        return Move.builder()
                .fromRow(moveWsDto.getFromRow())
                .fromCol(moveWsDto.getFromCol())
                .toRow(moveWsDto.getToRow())
                .toCol(moveWsDto.getToCol())
                .build();
    }

}
