package org.dama.damajatek.service;

import org.springframework.security.core.Authentication;

public interface IGameConnectionHandler {
    void handleDisconnect(String email, Authentication auth);
    void handleReconnect(String email, Authentication auth);
}
