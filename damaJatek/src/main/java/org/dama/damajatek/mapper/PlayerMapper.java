package org.dama.damajatek.mapper;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.entity.player.BotPlayer;
import org.dama.damajatek.entity.player.HumanPlayer;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.BotDifficulty;

public class PlayerMapper {

    public static Player createHumanPlayer(AppUser appUser) {
        return HumanPlayer.builder()
                .user(appUser)
                .build();
    }

    public static Player createBotPlayer(BotDifficulty botDifficulty) {
        return BotPlayer.builder()
                .difficulty(botDifficulty)
                .build();
    }

}
