package org.dama.damajatek.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUserInfoDto {

    private Long id;
    private String displayName;
    private String email;

    private Integer hostedRoomNum;
    private Integer joinedRoomNum;

    private Integer vsAiWins;
    private Integer vsAiLoses;
    private Integer vsAiDraws;
    private Integer vsAiGames;

    private Integer vsPlayerWins;
    private Integer vsPlayerLoses;
    private Integer vsPlayerDraws;
    private Integer vsPlayerGames;

    private Integer overallGames;

    private Integer vsBotWinrate;
    private Integer vsPlayerWinrate;

    private Integer overallWinrate;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
