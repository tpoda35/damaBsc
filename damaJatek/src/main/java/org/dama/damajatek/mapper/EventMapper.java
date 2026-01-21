package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.websocket.*;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Move;

import java.util.List;

public class EventMapper {

    public static MoveMadeEvent createMoveMadeEvent(Move move) {
        return MoveMadeEvent.builder()
                .move(move)
                .build();
    }

    public static NextTurnEvent createNextTurnEvent(PieceColor currentTurn, List<Move> allowedMoves) {
        return NextTurnEvent.builder()
                .currentTurn(currentTurn)
                .allowedMoves(allowedMoves)
                .build();
    }

    public static CaptureMadeEvent createCaptureMadeEvent(Move move) {
        return CaptureMadeEvent.builder()
                .move(move)
                .build();
    }

    public static PromotedPieceEvent createPromotedPieceEvent(Move move, PieceColor pieceColor) {
        return PromotedPieceEvent.builder()
                .row(move.getToRow())
                .col(move.getToCol())
                .pieceColor(pieceColor)
                .build();
    }

    public static GameOverEvent createGameOverEvent(String winnerName, PieceColor winnerColor, GameResult gameResult) {
        return GameOverEvent.builder()
                .winnerName(winnerName)
                .winnerColor(winnerColor)
                .gameResult(gameResult)
                .build();
    }

    public static GameDrawEvent createGameDrawEvent() {
        return GameDrawEvent.builder().build();
    }

    public static GameDrawEvent createGameDrawEvent(String drawReason) {
        return GameDrawEvent.builder()
                .drawReason(drawReason)
                .build();
    }

    public static GameForfeitEvent createGameForfeitEvent(String winnerName, PieceColor winnerColor, GameResult gameResult, String message) {
        return GameForfeitEvent.builder()
                .winnerName(winnerName)
                .winnerColor(winnerColor)
                .gameResult(gameResult)
                .message(message)
                .build();
    }

    public static RemovedPieceEvent createRemovedPieceEvent(PieceColor pieceColor) {
        return RemovedPieceEvent.builder()
                .pieceColor(pieceColor)
                .build();
    }

}
