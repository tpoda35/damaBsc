package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.GameWsAction;
import org.dama.damajatek.enums.game.PieceColor;

import static org.dama.damajatek.enums.game.GameWsAction.GAME_OVER;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameOverEvent implements IGameEvent {

    @Builder.Default
    private GameWsAction action = GAME_OVER;

    private String winnerName;
    private PieceColor winnerColor;
    private GameResult gameResult;

}
