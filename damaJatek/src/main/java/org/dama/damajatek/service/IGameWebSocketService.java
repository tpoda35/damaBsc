package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.websocket.GameEvent;

import java.security.Principal;

public interface IGameWebSocketService {
    void broadcastGameUpdate(GameEvent event, Principal principal, Long gameId);
    void sendErrorToPlayer(Principal principal, String errorMsg);
}
