package org.dama.damajatek.mapper;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.appUser.AppUserInfoDtoV1;
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

    public static RoomWsDto createRoomWsDto(RoomWsAction action) {
        return RoomWsDto.builder()
                .action(action)
                .build();
    }

    private static AppUserInfoDtoV1 createAppUserInfoDtoV1(AppUser appUser, ReadyStatus readyStatus) {
        if (appUser == null) {
            return null;
        }

        return AppUserInfoDtoV1.builder()
                .id(appUser.getId())
                .displayName(appUser.getDisplayName())
                .readyStatus(readyStatus)
                .build();
    }

}
