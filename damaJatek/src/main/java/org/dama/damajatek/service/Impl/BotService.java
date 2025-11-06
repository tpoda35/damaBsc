package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.bot.EasyBot;
import org.dama.damajatek.bot.IBotStrategy;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.service.IBotService;
import org.dama.damajatek.service.IGameEngine;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService implements IBotService {

    private final IGameEngine gameEngine;

    @Override
    public IBotStrategy getBot(BotDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new EasyBot(gameEngine);
            case MEDIUM -> null;
            case HARD -> null;
        };
    }
}
