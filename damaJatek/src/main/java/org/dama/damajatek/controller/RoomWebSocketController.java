package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.room.websocket.RoomRequestWsDto;
import org.dama.damajatek.service.IRoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RoomWebSocketController {

    private final IRoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roomId}")
    public void handleRoomMessage(
            @DestinationVariable Long roomId,
            @Payload RoomRequestWsDto payload
    ) {
    }
}
