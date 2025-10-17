package org.dama.damajatek.dto.game.websocket;

import lombok.Data;

@Data
public class MoveWsDto {
    private Long gameId;
    private Long playerId; // optional if you use security principal
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
}
