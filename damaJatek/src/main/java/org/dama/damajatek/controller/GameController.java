package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.service.IGameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/games")
@Slf4j
@RequiredArgsConstructor
public class GameController {

    private final IGameService gameService;

    @GetMapping("/{gameId}")
    public CompletableFuture<GameInfoDtoV1> getGameInfo(@PathVariable("gameId") Long gameId) {
        return gameService.getGameInfo(gameId);
    }

}
