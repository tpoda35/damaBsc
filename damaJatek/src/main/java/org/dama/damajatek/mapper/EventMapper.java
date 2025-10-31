package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.websocket.CaptureMadeEventDto;
import org.dama.damajatek.dto.game.websocket.MoveMadeEventDto;
import org.dama.damajatek.dto.game.websocket.NextTurnEventDto;
import org.dama.damajatek.dto.game.websocket.PromotedPieceEventDto;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Move;

import java.util.List;

public class EventMapper {

    public static MoveMadeEventDto createMoveMadeEventDto(Move move) {
        return MoveMadeEventDto.builder()
                .move(move)
                .build();
    }

    public static NextTurnEventDto createNextTurnEventDto(PieceColor currentTurn, List<Move> allowedMoves) {
        return NextTurnEventDto.builder()
                .currentTurn(currentTurn)
                .allowedMoves(allowedMoves)
                .build();
    }

    public static CaptureMadeEventDto createCaptureMadeEventDto(Move move) {
        return CaptureMadeEventDto.builder()
                .move(move)
                .build();
    }

    public static PromotedPieceEventDto createPromotedPieceEventDto(Move move, PieceColor pieceColor) {
        return PromotedPieceEventDto.builder()
                .row(move.getToRow())
                .col(move.getToCol())
                .pieceColor(pieceColor)
                .build();
    }

}
