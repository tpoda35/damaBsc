package org.dama.damajatek.service;

import org.dama.damajatek.bot.IBotStrategy;
import org.dama.damajatek.enums.game.BotDifficulty;

public interface IBotService {
    IBotStrategy getBot(BotDifficulty difficulty);
}
