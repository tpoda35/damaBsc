package org.dama.damajatek.mapper;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.appUser.AppUserInfoDtoV1;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.entity.Room;

import static org.dama.damajatek.enums.room.ReadyStatus.NOT_READY;

public class RoomMapper {

    public static Room createRoom(RoomCreateDto roomCreateDto, AppUser host, String encodedPassword) {
        return Room.builder()
                .name(roomCreateDto.getName())
                .locked(roomCreateDto.isLocked())
                .password(encodedPassword)
                .host(host)
                .build();
    }

    public static RoomInfoDtoV1 createRoomInfoDtoV1(Room room, AppUser host, AppUser opponent, boolean isHost) {
        return RoomInfoDtoV1.builder()
                .id(room.getId())
                .name(room.getName())
                .isHost(isHost)
                .host(createAppUserInfoDtoV1(host))
                .opponent(createAppUserInfoDtoV1(opponent))
                .build();
    }

    public static RoomInfoDtoV2 createRoomInfoDtoV2(Room room) {
        return RoomInfoDtoV2.builder()
                .id(room.getId())
                .name(room.getName())
                .playerCount(room.getOpponent() != null ? 2 : 1)
                .locked(room.isLocked())
                .build();
    }

    private static AppUserInfoDtoV1 createAppUserInfoDtoV1(AppUser appUser) {
        if (appUser == null) {
            return null;
        }

        return AppUserInfoDtoV1.builder()
                .id(appUser.getId())
                .displayName(appUser.getDisplayName())
                .readyStatus(NOT_READY)
                .build();
    }

}
