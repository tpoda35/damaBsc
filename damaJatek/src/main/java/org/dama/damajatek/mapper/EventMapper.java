package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.websocket.MoveMadeEventDto;
import org.dama.damajatek.dto.game.websocket.NextTurnEventDto;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Move;

import java.util.List;

public class EventMapper {

    public static MoveMadeEventDto createMoveMadeEventDto(Move move) {
        return MoveMadeEventDto.builder()
                .fromRow(move.getFromRow())
                .fromCol(move.getFromCol())
                .toRow(move.getToRow())
                .toCol(move.getToCol())
                .build();
    }

    public static NextTurnEventDto createNextTurnEventDto(PieceColor currentTurn, List<Move> allowedMoves) {
        return NextTurnEventDto.builder()
                .currentTurn(currentTurn)
                .allowedMoves(allowedMoves)
                .build();
    }

}
