package org.dama.damajatek.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.service.IGameConnectionHandler;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectListener {

    private final IGameConnectionHandler gameConnectionHandler;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        Object userPrincipal = accessor.getUser();

        log.info("WebSocket connected. Session ID: {}", sessionId);

        if (userPrincipal instanceof Authentication auth) {
            String email = auth.getName();

            try {
                gameConnectionHandler.handleReconnect(email, auth);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.debug("Optimistic locking exception at disconnect for user: {}", email);
            } catch (Exception e) {
                log.error("Error handling reconnect for user: {}", email, e);
            }
        } else {
            log.warn("Could not identify user for session: {}", sessionId);
        }
    }

}
