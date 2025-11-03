package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.websocket.*;
import org.dama.damajatek.enums.game.GameResult;
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

    public static GameOverEventDto createGameOverEventDto(String winnerName, GameResult gameResult) {
        return GameOverEventDto.builder()
                .winnerName(winnerName)
                .gameResult(gameResult)
                .build();
    }

    public static GameDrawEventDto createGameDrawEventDto() {
        return GameDrawEventDto.builder().build();
    }

    public static GameDrawEventDto createGameDrawEventDto(String drawReason) {
        return GameDrawEventDto.builder()
                .drawReason(drawReason)
                .build();
    }

    public static GameForfeitEventDto createGameForfeitEventDto(String winnerName, GameResult gameResult, String message) {
        return GameForfeitEventDto.builder()
                .winnerName(winnerName)
                .gameResult(gameResult)
                .message(message)
                .build();
    }

}
