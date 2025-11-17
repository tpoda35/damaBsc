package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.GameHistoryDto;
import org.dama.damajatek.service.IGameService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/profiles")
@Slf4j
@RequiredArgsConstructor
public class ProfileController {

    private final IGameService gameService;

    @GetMapping("/game-history")
    public CompletableFuture<Page<GameHistoryDto>> getGameHistory() {
        return gameService.getGameHistory();
    }

}
