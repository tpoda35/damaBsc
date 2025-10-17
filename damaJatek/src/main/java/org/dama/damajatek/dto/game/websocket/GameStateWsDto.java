package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dama.damajatek.enums.game.GameStatus;
import org.dama.damajatek.enums.game.PieceColor;

@Data
@AllArgsConstructor
public class GameStateWsDto {
    private Long gameId;
    private String boardJson;         // serialized Board (client will parse)
    private PieceColor currentTurn;
    private GameStatus status;
    private String winnerName;        // null unless finished
}
