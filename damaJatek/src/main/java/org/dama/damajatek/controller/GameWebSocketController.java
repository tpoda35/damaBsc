package org.dama.damajatek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.game.ErrorMessage;
import org.dama.damajatek.dto.game.GameStateMessage;
import org.dama.damajatek.dto.game.MoveMessage;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.GameStatus;
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

    @MessageMapping("/game/{gameId}/move")
    public void handleMove(
            @DestinationVariable Long gameId,
            @Payload MoveMessage moveMsg,
            Principal principal
    ) {
        try {
            // Build the service Move model from DTO
            Move move = Move.builder()
                    .fromRow(moveMsg.getFromRow())
                    .fromCol(moveMsg.getFromCol())
                    .toRow(moveMsg.getToRow())
                    .toCol(moveMsg.getToCol())
                    .build();

            Game updated = gameService.makeMove(gameId, move);

            GameStateMessage state = getGameStateMessage(updated);

            template.convertAndSend("/topic/games." + gameId, state);

        } catch (IllegalArgumentException ex) {
            log.warn("Move rejected: {}", ex.getMessage());
            sendErrorToPlayer(principal, "/topic/games." + gameId, ex.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error handling move", e);
            sendErrorToPlayer(principal, "/topic/games." + gameId, "Server error handling move");
        }
    }

    private static GameStateMessage getGameStateMessage(Game updated) {
        String boardJson = updated.getBoardState();

        String winnerName = null;
        if (updated.getStatus() == GameStatus.FINISHED && updated.getWinner() != null) {
            winnerName = updated.getWinner().getDisplayName();
        }

        return new GameStateMessage(
                updated.getId(),
                boardJson,
                updated.getCurrentTurn(),
                updated.getStatus(),
                winnerName
        );
    }

    private void sendErrorToPlayer(Principal principal, String fallbackTopic, String errorMsg) {
        ErrorMessage err = new ErrorMessage(errorMsg);
        try {
            if (principal != null) {
                template.convertAndSendToUser(principal.getName(), "/queue/errors", err);
            } else {
                template.convertAndSend(fallbackTopic + ".errors", err);
            }
        } catch (Exception ex) {
            log.error("Failed to send websocket error message", ex);
        }
    }
}
