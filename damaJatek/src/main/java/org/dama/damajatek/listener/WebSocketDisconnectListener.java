package org.dama.damajatek.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.service.IRoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final IRoomService roomService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        Object userPrincipal = accessor.getUser();

        log.info("WebSocket disconnected. Session ID: {}", sessionId);

        if (userPrincipal instanceof Authentication auth) {
            String email = auth.getName();

            try {
                roomService.handleUserDisconnect(email);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.debug("Room already cleaned up for user: {} (likely explicit leave)", email);
            } catch (Exception e) {
                log.error("Error handling disconnect for user: {}", email, e);
            }
        } else {
            log.warn("Could not identify user for session: {}", sessionId);
        }
    }
}
