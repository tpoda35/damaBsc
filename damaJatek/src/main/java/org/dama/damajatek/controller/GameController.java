package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.game.AiGameCreateDto;
import org.dama.damajatek.dto.game.ForfeitRequest;
import org.dama.damajatek.dto.game.GameInfoDto;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.mapper.PlayerMapper;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.service.Impl.GameWebSocketService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/games")
@Slf4j
@RequiredArgsConstructor
public class GameController {

    private final IGameService gameService;
    private final GameWebSocketService gameWebSocketService;
    private final IAppUserService appUserService;

    @GetMapping("/{gameId}")
    public CompletableFuture<GameInfoDto> getGame(@PathVariable("gameId") Long gameId) {
        return gameService.getGame(gameId);
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

    @PostMapping("/ai/start")
    public Long startAiGame(
            @RequestBody AiGameCreateDto aiGameCreateDto
    ) {
        AppUser loggedInUser = appUserService.getLoggedInUser();

        Player redPlayer;
        Player blackPlayer;

        Random random = new Random();
        int number = random.nextInt(2) + 1;
        if (number == 1) {
            blackPlayer = PlayerMapper.createBotPlayer(aiGameCreateDto.getBotDifficulty());
            redPlayer = PlayerMapper.createHumanPlayer(loggedInUser);
        } else {
            blackPlayer = PlayerMapper.createHumanPlayer(loggedInUser);
            redPlayer = PlayerMapper.createBotPlayer(aiGameCreateDto.getBotDifficulty());
        }

        Game game = gameService.createGame(redPlayer, blackPlayer, null);
        return game.getId();
    }

}
