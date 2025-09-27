package org.dama.damajatek.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.ErrorMessage;
import org.dama.damajatek.dto.game.GameStateMessage;
import org.dama.damajatek.dto.game.MoveMessage;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.GameStatus;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.service.IGameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketController {

    private final SimpMessagingTemplate template;
    private final IGameService gameService;
    private final ObjectMapper objectMapper;

    /**
     * Clients should send to /app/game.{gameId}.move
     * We'll route this method using @MessageMapping with a path variable.
     */
    @MessageMapping("/game/{gameId}/move")
    public void handleMove(@DestinationVariable Long gameId,
                           @Payload MoveMessage moveMsg,
                           Principal principal) {
        try {
            // Build the service Move model from DTO
            Move move = Move.builder()
                    .fromRow(moveMsg.getFromRow())
                    .fromCol(moveMsg.getFromCol())
                    .toRow(moveMsg.getToRow())
                    .toCol(moveMsg.getToCol())
                    .build();

            // Optional: map principal to AppUser check here. If you don't have auth,
            // rely on moveMsg.playerId and validate it matches Game players.
            // Make move - GameService.handleMove returns saved Game entity
            Game updated = gameService.makeMove(gameId, move);

            // Load board and broadcast new state to subscribers
            Board board = objectMapper.readValue(updated.getBoardState(), Board.class);

            String boardJson = updated.getBoardState();

            String winnerName = null;
            if (updated.getStatus() == GameStatus.FINISHED && updated.getWinner() != null) {
                winnerName = updated.getWinner().getUsername(); // example getter
            }

            GameStateMessage state = new GameStateMessage(
                    updated.getId(),
                    boardJson,
                    updated.getCurrentTurn(),
                    updated.getStatus(),
                    winnerName
            );

            // Broadcast to topic for this game. Clients subscribe to /topic/games.{gameId}
            template.convertAndSend("/topic/games." + gameId, state);

        } catch (IllegalArgumentException ex) {
            // GameService raised validation (invalid move, forced capture, etc).
            log.warn("Move rejected: {}", ex.getMessage());
            sendErrorToPlayer(principal, "/topic/games." + gameId, ex.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error handling move", e);
            sendErrorToPlayer(principal, "/topic/games." + gameId, "Server error handling move");
        }
    }

    private void sendErrorToPlayer(Principal principal, String fallbackTopic, String errorMsg) {
        ErrorMessage err = new ErrorMessage(errorMsg);
        try {
            if (principal != null) {
                // send to user-specific queue
                template.convertAndSendToUser(principal.getName(), "/queue/errors", err);
            } else {
                // No authenticated user — push to fallback topic for the game so frontends can display
                template.convertAndSend(fallbackTopic + ".errors", err);
            }
        } catch (Exception ex) {
            log.error("Failed to send websocket error message", ex);
        }
    }
}
