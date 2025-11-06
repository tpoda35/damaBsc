package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.websocket.IGameEvent;

import java.security.Principal;

public interface IGameWebSocketService {
    void broadcastGameUpdate(IGameEvent event, Principal principal, Long gameId);
    void sendErrorToPlayer(Principal principal, String errorMsg);
}
