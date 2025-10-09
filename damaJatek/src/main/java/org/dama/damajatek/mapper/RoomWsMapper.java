package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.room.websocket.RoomWsDto;

public class RoomWsMapper {

    public static RoomWsDto createRoomWsDto() {
        return RoomWsDto.builder().build();
    }

}
