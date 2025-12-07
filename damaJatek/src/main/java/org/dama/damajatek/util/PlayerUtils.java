package org.dama.damajatek.util;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.exception.auth.AccessDeniedException;

public class PlayerUtils {

    public static boolean isHumanPlayerMatchingUser(Player player, AppUser user) {
        Long playerUserId = player.getAppUserId();
        return playerUserId != null && playerUserId.equals(user.getId());
    }

    public static void verifyUserAccess(Game game, AppUser user) {
        if (!isHumanPlayerMatchingUser(game.getRedPlayer(), user)
                && !isHumanPlayerMatchingUser(game.getBlackPlayer(), user)) {
            throw new AccessDeniedException("You are not a participant in this game");
        }
    }
}
