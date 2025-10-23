package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveWsDto {

    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

}
