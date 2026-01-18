package org.dama.damajatek.service;

import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;

import java.security.Principal;

public interface IChatWebSocketService {
    void broadcastChatMessage(ChatMessageResponseDto chatMessageResponseDto, Principal principal, Long roomId);
    void sendErrorToPlayer(Principal principal, String errorMsg);
}
