package org.dama.damajatek.service;

import org.dama.damajatek.model.Move;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.authentication.user.AppUser;

public interface IGameService {
    Game createGame(AppUser redPlayer, AppUser blackPlayer, boolean vsBot, BotDifficulty difficulty);
    Game makeMove(Long gameId, Move move);
}
