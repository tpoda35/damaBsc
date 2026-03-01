package org.dama.damajatek.service;

import org.dama.damajatek.bot.IBotStrategy;
import org.dama.damajatek.enums.game.BotDifficulty;

public interface IBotCacheService {
    IBotStrategy getOrCreateBot(Long gameId, BotDifficulty difficulty);
}
