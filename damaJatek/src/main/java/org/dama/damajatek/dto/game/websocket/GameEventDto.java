package org.dama.damajatek.dto.game.websocket;

import org.dama.damajatek.enums.game.GameWsAction;

public interface GameEventDto {
    GameWsAction getAction();
}
