package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dama.damajatek.enums.GameStatus;
import org.dama.damajatek.enums.PieceColor;

@Data
@AllArgsConstructor
public class GameStateMessage {
    private Long gameId;
    private String boardJson;         // serialized Board (client will parse)
    private PieceColor currentTurn;
    private GameStatus status;
    private String winnerName;        // null unless finished
}
