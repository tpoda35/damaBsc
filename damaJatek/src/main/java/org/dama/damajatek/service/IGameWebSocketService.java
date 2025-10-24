package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.websocket.GameEventDto;

import java.security.Principal;

public interface IGameWebSocketService {
    void broadcastGameUpdate(GameEventDto event, Principal principal,Long gameId);
}
