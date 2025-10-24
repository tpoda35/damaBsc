package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;

import static org.dama.damajatek.enums.game.GameWsAction.MOVE_MADE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveMadeEventDto implements GameEventDto {
    @Builder.Default
    private GameWsAction action = MOVE_MADE;

    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
}
