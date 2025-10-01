package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.entity.Room;

public class RoomMapper {

    public static RoomInfoDtoV1 createRoomInfoDtoV1(Room room) {
        return RoomInfoDtoV1.builder()
                .id(room.getId())
                .name(room.getName())
                .host(room.getHost().getDisplayName())
                .started(room.isStarted())
                .locked(room.isLocked())
                .build();
    }

    public static RoomInfoDtoV2 createRoomInfoDtoV2(Room room) {
        return RoomInfoDtoV2.builder()
                .id(room.getId())
                .name(room.getName())
                .host(room.getHost().getDisplayName())
                .playerCount(room.getOpponent() != null ? 2 : 1)
                .started(room.isStarted())
                .locked(room.isLocked())
                .build();
    }

}
