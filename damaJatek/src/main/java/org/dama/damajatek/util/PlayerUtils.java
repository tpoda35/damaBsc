package org.dama.damajatek.util;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.HumanPlayer;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.exception.auth.AccessDeniedException;

public class PlayerUtils {

    public static boolean isHumanPlayerMatchingUser(Player player, AppUser user) {
        if (player == null || user == null) return false;
        if (player instanceof HumanPlayer humanPlayer) {
            AppUser humanUser = humanPlayer.getUser();
            return humanUser != null && humanUser.getId().equals(user.getId());
        }
        return false;
    }

    public static void verifyUserAccess(Game game, AppUser user) {
        if (!isHumanPlayerMatchingUser(game.getRedPlayer(), user)
                && !isHumanPlayerMatchingUser(game.getBlackPlayer(), user)) {
            throw new AccessDeniedException("You are not a participant in this game");
        }
    }


}
