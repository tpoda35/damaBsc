package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;

public class ChatMessageMapper {

    public static ChatMessageResponseDto createChatMessageResponseDto(Long senderId, String content) {
         return ChatMessageResponseDto.builder()
                 .senderId(senderId)
                 .content(content)
                 .build();
    }

}
