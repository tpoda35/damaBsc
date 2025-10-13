package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.room.websocket.RoomWsDto;
import org.dama.damajatek.enums.room.ReadyStatus;
import org.dama.damajatek.enums.room.RoomWsAction;
import org.dama.damajatek.mapper.RoomWsMapper;
import org.dama.damajatek.service.IRoomWebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomWebSocketService implements IRoomWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // The player is usually the opponent
    @Override
    public void broadcastRoomUpdate(ReadyStatus readyStatus, AppUser player, RoomWsAction action, String destination) {
        RoomWsDto message = RoomWsMapper.createRoomWsDto(action, player, readyStatus);

        log.info("Broadcasting room update: action={}, destination={}", action, destination);

        messagingTemplate.convertAndSend(destination, message);
    }

    @Override
    public void broadcastRoomUpdate(RoomWsAction action, String destination) {
        RoomWsDto message = RoomWsMapper.createRoomWsDto(action);

        log.info("Broadcasting room update: action={}, destination={}", action, destination);

        messagingTemplate.convertAndSend(destination, message);
    }
}
