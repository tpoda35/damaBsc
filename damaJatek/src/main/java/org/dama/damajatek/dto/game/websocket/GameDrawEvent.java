package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;

import static org.dama.damajatek.enums.game.GameWsAction.GAME_DRAW;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameDrawEvent implements GameEvent {

    @Builder.Default
    private GameWsAction action = GAME_DRAW;

    private String drawReason;

}
