package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.room.chat.ChatMessageRequestDto;
import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;
import org.dama.damajatek.service.IChatService;
import org.dama.damajatek.service.IChatWebSocketService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final IChatService chatService;
    private final IChatWebSocketService chatWebSocketService;

    @MessageMapping("/room/{roomId}/chat/send")
    public void send(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequestDto dto,
            Principal principal
    ) {
        ChatMessageResponseDto chatMessageResponseDto = chatService.handleMessage(
                roomId,
                principal,
                dto.getContent()
        );

        chatWebSocketService.broadcastChatMessage(chatMessageResponseDto, principal, roomId);
    }
}
