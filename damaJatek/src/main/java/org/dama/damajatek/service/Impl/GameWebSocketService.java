//package org.dama.damajatek.service.Impl;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.dama.damajatek.dto.game.ErrorMessage;
//import org.dama.damajatek.dto.game.websocket.GameWsDto;
//import org.dama.damajatek.model.Move;
//import org.dama.damajatek.service.IGameWebSocketService;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.security.Principal;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class GameWebSocketService implements IGameWebSocketService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @Override
//    public void broadcastGameUpdate(Move move, GameWsDto gameWsDto, Principal principal) {
//        try {
//
//            GameWsDto state = getGameStateMessage(updated);
//
//            template.convertAndSend("/topic/games." + gameId, state);
//
//        } catch (IllegalArgumentException ex) {
//            log.warn("Move rejected: {}", ex.getMessage());
//            sendErrorToPlayer(principal, "/topic/games." + gameId, ex.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected error handling move", e);
//            sendErrorToPlayer(principal, "/topic/games." + gameId, "Server error handling move");
//        }
//    }
//
//    private void sendErrorToPlayer(Principal principal, String fallbackTopic, String errorMsg) {
//        ErrorMessage err = new ErrorMessage(errorMsg);
//        try {
//            if (principal != null) {
//                messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", err);
//            } else {
//                messagingTemplate.convertAndSend(fallbackTopic + ".errors", err);
//            }
//        } catch (Exception ex) {
//            log.error("Failed to send websocket error message", ex);
//        }
//    }
//
//
//}
