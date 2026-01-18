package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;
import org.dama.damajatek.entity.room.ChatMessage;
import org.dama.damajatek.entity.room.Room;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.room.RoomNotFoundException;
import org.dama.damajatek.mapper.ChatMessageMapper;
import org.dama.damajatek.repository.IChatMessageRepository;
import org.dama.damajatek.repository.IRoomRepository;
import org.dama.damajatek.service.IChatService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final IRoomRepository roomRepository;
    private final IChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessageResponseDto handleMessage(Long roomId, Principal principal, String content) {
        Authentication auth = (Authentication) principal;
        AppUser sender = (AppUser) auth.getPrincipal();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        if (!isMember(room, sender)) {
            throw new AccessDeniedException("User not in room");
        }

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .build();

        chatMessageRepository.save(message);

        return ChatMessageMapper.createChatMessageResponseDto(sender.getId(), content);
    }

    private boolean isMember(Room room, AppUser user) {
        return user.getId().equals(room.getHost().getId())
                || user.getId().equals(room.getOpponent().getId());
    }
}
