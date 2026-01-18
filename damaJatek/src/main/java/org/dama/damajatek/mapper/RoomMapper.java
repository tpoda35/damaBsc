package org.dama.damajatek.mapper;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.appUser.AppUserGameDto;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.entity.room.Room;
import org.dama.damajatek.enums.room.ReadyStatus;

public class RoomMapper {

    public static Room createRoom(RoomCreateDto roomCreateDto, AppUser host, String encodedPassword) {
        return Room.builder()
                .name(roomCreateDto.getName())
                .description(roomCreateDto.getDescription())
                .locked(roomCreateDto.getLocked())
                .password(encodedPassword)
                .host(host)
                .build();
    }

    public static RoomInfoDtoV1 createRoomInfoDtoV1(Room room, AppUser host, AppUser opponent, boolean isHost) {
        return RoomInfoDtoV1.builder()
                .id(room.getId())
                .name(room.getName())
                .isHost(isHost)
                .host(createAppUserInfoDtoV1(host, room.getHostReadyStatus()))
                .opponent(createAppUserInfoDtoV1(opponent, room.getOpponentReadyStatus()))
                .build();
    }

    public static RoomInfoDtoV2 createRoomInfoDtoV2(Room room) {
        return RoomInfoDtoV2.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .playerCount(room.getOpponent() != null ? 2 : 1)
                .locked(room.getLocked())
                .build();
    }

    private static AppUserGameDto createAppUserInfoDtoV1(AppUser appUser, ReadyStatus readyStatus) {
        if (appUser == null) {
            return null;
        }

        return AppUserGameDto.builder()
                .id(appUser.getId())
                .displayName(appUser.getDisplayName())
                .readyStatus(readyStatus)
                .build();
    }

}
