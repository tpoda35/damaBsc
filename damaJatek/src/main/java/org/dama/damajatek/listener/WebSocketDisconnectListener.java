package org.dama.damajatek.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.websocket.disconnect.DisconnectDto;
import org.dama.damajatek.service.IGameConnectionHandler;
import org.dama.damajatek.service.IGameWebSocketService;
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
    private final IGameConnectionHandler gameConnectionHandler;
    private final IGameWebSocketService gameWebSocketService;

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
                DisconnectDto disconnectDto = gameConnectionHandler.handleDisconnect(email);
                gameWebSocketService.broadcastGameUpdate(
                        disconnectDto.getGameEvent(), auth, disconnectDto.getGameId()
                );
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
