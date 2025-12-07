package org.dama.damajatek.mapper;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.dto.appUser.AppUserGameStats;

public class AppUserMapper {

    public static AppUserInfoDto createAppUserInfoDto(AppUser appUser, AppUserGameStats stats) {
        return AppUserInfoDto.builder()
                .id(appUser.getId())
                .displayName(appUser.getDisplayName())
                .email(appUser.getEmail())

                .hostedRoomNum(stats.hostedRooms())
                .joinedRoomNum(stats.joinedRooms())

                .vsAiWins(stats.vsAiWins())
                .vsAiLoses(stats.vsAiLoses())
                .vsAiDraws(stats.vsAiDraws())
                .vsAiGames(stats.vsAiGames())

                .vsPlayerWins(stats.vsPlayerWins())
                .vsPlayerLoses(stats.vsPlayerLoses())
                .vsPlayerDraws(stats.vsPlayerDraws())
                .vsPlayerGames(stats.vsPlayerGames())

                .overallGames(stats.overallGames())

                .vsBotWinrate(stats.vsBotWinrate())
                .vsPlayerWinrate(stats.vsPlayerWinrate())

                .overallWinrate(stats.overallWinrate())

                .createdAt(appUser.getCreatedAt())
                .updatedAt(appUser.getUpdatedAt())
                .build();
    }

}
