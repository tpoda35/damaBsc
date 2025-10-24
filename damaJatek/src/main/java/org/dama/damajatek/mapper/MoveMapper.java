package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.MoveDto;
import org.dama.damajatek.model.Move;

public class MoveMapper {

    public static Move createMove(MoveDto moveDto) {
        return Move.builder()
                .fromRow(moveDto.getFromRow())
                .fromCol(moveDto.getFromCol())
                .toRow(moveDto.getToRow())
                .toCol(moveDto.getToCol())
                .build();
    }

}
