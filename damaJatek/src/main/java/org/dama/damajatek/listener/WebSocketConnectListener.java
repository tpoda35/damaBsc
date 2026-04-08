package org.dama.damajatek.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectListener {

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object userPrincipal = accessor.getUser();

        if (userPrincipal instanceof Authentication auth) {
            String email = auth.getName();

        }
    }

}
