package org.dama.damajatek.dto.game;

import lombok.Data;

@Data
public class MoveMessage {
    private Long gameId;
    private Long playerId; // optional if you use security principal
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
}
