package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.websocket.disconnect.DisconnectDto;

public interface IGameConnectionHandler {
    DisconnectDto handleDisconnect(String email);
    void handleReconnect(String email);
}
