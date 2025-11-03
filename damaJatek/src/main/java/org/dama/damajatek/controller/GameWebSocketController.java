package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.MoveDto;
import org.dama.damajatek.dto.game.websocket.GameEvent;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.game.GameAlreadyFinishedException;
import org.dama.damajatek.exception.game.InvalidMoveException;
import org.dama.damajatek.mapper.MoveMapper;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.service.IGameWebSocketService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketController {

    private final IGameWebSocketService gameWebSocketService;
    private final IGameService gameService;

    @MessageMapping("/games/{gameId}/move")
    public void handleMove(
            @DestinationVariable Long gameId,
            @Payload MoveDto moveDto,
            Principal principal
    ) {
        Move move = MoveMapper.createMove(moveDto);

        try {
            List<GameEvent> events = gameService.makeMove(gameId, move, principal);

            for (GameEvent event : events) {
                gameWebSocketService.broadcastGameUpdate(event, principal, gameId);
            }

        } catch (InvalidMoveException ex) {
            gameWebSocketService.sendErrorToPlayer(principal, "Invalid move");

        } catch (GameAlreadyFinishedException ex) {
            gameWebSocketService.sendErrorToPlayer(principal, "Game is already finished");

        } catch (AccessDeniedException ex) {
            gameWebSocketService.sendErrorToPlayer(principal, ex.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error while making move in game {}: {}", gameId, ex.getMessage(), ex);
            gameWebSocketService.sendErrorToPlayer(principal, "An unexpected error occurred. Please try again.");
        }
    }
}
