package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.bot.EasyBot;
import org.dama.damajatek.bot.HardBot;
import org.dama.damajatek.bot.IBotStrategy;
import org.dama.damajatek.bot.MediumBot;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.service.IBotCacheService;
import org.dama.damajatek.service.IGameEngine;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotCacheService implements IBotCacheService {

    private final IGameEngine gameEngine;

    // Eviction does not needed, bcs 30 minutes ttl
    @Cacheable(value = "botCache", key = "#gameId")
    @Override
    public IBotStrategy getOrCreateBot(Long gameId, BotDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new EasyBot(gameEngine);
            case MEDIUM -> new MediumBot(gameEngine);
            case HARD -> new HardBot(gameEngine);
        };
    }
}
