package org.dama.damajatek.mapper;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.appUser.AppUserGameDto;
import org.dama.damajatek.dto.room.websocket.RoomWsDto;
import org.dama.damajatek.enums.room.ReadyStatus;
import org.dama.damajatek.enums.room.RoomWsAction;

public class RoomWsMapper {

    public static RoomWsDto createRoomWsDto(RoomWsAction action, AppUser player, ReadyStatus readyStatus) {
        return RoomWsDto.builder()
                .action(action)
                .player(createAppUserInfoDtoV1(player, readyStatus))
                .build();
    }

    public static RoomWsDto createRoomWsDto(RoomWsAction action, Long gameId) {
        return RoomWsDto.builder()
                .action(action)
                .gameId(gameId)
                .build();
    }

    public static RoomWsDto createRoomWsDto(RoomWsAction action) {
        return RoomWsDto.builder()
                .action(action)
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
