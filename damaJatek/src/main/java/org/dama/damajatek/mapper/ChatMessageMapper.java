package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;
import org.dama.damajatek.entity.room.ChatMessage;

import java.util.List;

public class ChatMessageMapper {

    // message.sender must be eagerly loaded
    public static ChatMessageResponseDto createChatMessageResponseDto(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getDisplayName())
                .content(message.getContent())
                .build();
    }

    public static List<ChatMessageResponseDto> createChatMessageResponseDtoList(List<ChatMessage> messages) {
        return messages.stream()
                .map(ChatMessageMapper::createChatMessageResponseDto)
                .toList();
    }

}
