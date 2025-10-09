package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.enums.room.RoomWsAction;
import org.dama.damajatek.service.IRoomWebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomWebSocketService implements IRoomWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastRoomUpdate(Long roomId, RoomWsAction action) {

    }
}
