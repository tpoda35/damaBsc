package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.websocket.GameWsDto;

import java.security.Principal;

public interface IGameWebSocketService {
    void broadcastGameUpdate(GameWsDto game, Principal principal);
}
