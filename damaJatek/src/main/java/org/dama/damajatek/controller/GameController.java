package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.ForfeitRequest;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.service.Impl.GameWebSocketService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/games")
@Slf4j
@RequiredArgsConstructor
public class GameController {

    private final IGameService gameService;
    private final GameWebSocketService gameWebSocketService;

    @GetMapping("/{gameId}")
    public CompletableFuture<GameInfoDtoV1> getGameInfo(@PathVariable("gameId") Long gameId) {
        return gameService.getGameInfo(gameId);
    }

    @PostMapping("/{gameId}/forfeit")
    public void forfeit(
            @PathVariable("gameId") Long gameId,
            @RequestBody ForfeitRequest forfeitRequest,
            Principal principal
    ) {
        gameWebSocketService.broadcastGameUpdate(
                gameService.forfeit(gameId, forfeitRequest.getPieceColor()),
                principal,
                gameId
        );
    }

//    @PostMapping("/ai/start")
//    public Long startAiGame()

}
