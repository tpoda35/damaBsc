package org.dama.damajatek.service;

import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;

import java.security.Principal;

public interface IChatService {
    ChatMessageResponseDto handleMessage(Long roomId, Principal sender, String content);
}
