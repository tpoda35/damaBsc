package org.dama.damajatek.service;

import org.dama.damajatek.model.Move;
import org.dama.damajatek.enums.BotDifficulty;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.security.user.AppUser;

public interface IGameService {
    Game createGame(String name, AppUser redPlayer, AppUser blackPlayer, boolean vsBot, BotDifficulty difficulty);
    Game makeMove(Long gameId, Move move);
}
