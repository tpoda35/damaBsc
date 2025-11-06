package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.ErrorMessage;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.service.IGameWebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameWebSocketService implements IGameWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastGameUpdate(IGameEvent event, Principal principal, Long gameId) {
        messagingTemplate.convertAndSend("/topic/games/" + gameId, event);
    }

    @Override
    public void sendErrorToPlayer(Principal principal, String errorMsg) {
        ErrorMessage err = new ErrorMessage(errorMsg);
        try {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", err);
        } catch (Exception ex) {
            log.error("Failed to send websocket error message", ex);
        }
    }
}
