package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.GameWsAction;

import static org.dama.damajatek.enums.game.GameWsAction.GAME_FORFEIT;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameForfeitEvent implements GameEvent {

    @Builder.Default
    private GameWsAction action = GAME_FORFEIT;

    private String winnerName;
    private GameResult gameResult;
    private String message;

}
