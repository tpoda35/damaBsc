package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.ErrorMessage;
import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;
import org.dama.damajatek.service.IChatWebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketService implements IChatWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastChatMessage(ChatMessageResponseDto chatMessageResponseDto, Principal principal, Long roomId) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", chatMessageResponseDto);
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
