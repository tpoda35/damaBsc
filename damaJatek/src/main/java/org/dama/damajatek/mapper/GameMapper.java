package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.GameHistoryDto;
import org.dama.damajatek.dto.game.GameInfoDto;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

import java.time.OffsetDateTime;
import java.util.List;

public class GameMapper {

    // The game players need to be loaded to use this
    public static GameInfoDto createGameInfoDtoV1(Game game, Board board, List<Move> allowedMoves, PieceColor playerColor) {
        return GameInfoDto.builder()
                .id(game.getId())
                .board(board)
                .currentTurn(game.getCurrentTurn())
                .allowedMoves(allowedMoves)
                .removedRedPieces(game.getRemovedRedPieces())
                .removedWhitePieces(game.getRemovedWhitePieces())
                .playerColor(playerColor)
                .enemyDisplayName(getEnemyDisplayName(game, playerColor))
                .build();
    }

    public static GameHistoryDto createGameHistoryDtoV1(Game game, OffsetDateTime gameTime) {
        return GameHistoryDto.builder()
                .id(game.getId())
                .redPlayer(game.getRedPlayer())
                .blackPlayer(game.getBlackPlayer())
                .status(game.getStatus())
                .result(game.getResult())
                .drawReason(game.getDrawReason())
                .winner(game.getWinner())
                .totalMoves(game.getTotalMoves())
                .startTime(game.getStartTime())
                .endTime(game.getEndTime())
                .gameTime(gameTime)
                .build();
    }

    private static String getEnemyDisplayName(Game game, PieceColor playerColor) {
        if (playerColor == PieceColor.RED) {
            return game.getBlackPlayer().getDisplayName();
        }
        return game.getRedPlayer().getDisplayName();
    }


}
