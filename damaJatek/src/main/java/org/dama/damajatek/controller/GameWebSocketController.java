//package org.dama.damajatek.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.dama.damajatek.dto.game.websocket.GameWsDto;
//import org.dama.damajatek.dto.game.websocket.MoveWsDto;
//import org.dama.damajatek.mapper.MoveMapper;
//import org.dama.damajatek.model.Move;
//import org.dama.damajatek.service.IGameService;
//import org.dama.damajatek.service.IGameWebSocketService;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//
//@Controller
//@RequiredArgsConstructor
//@Slf4j
//public class GameWebSocketController {
//
//    private final IGameWebSocketService gameWebSocketService;
//    private final IGameService gameService;
//
//    @MessageMapping("/games/{gameId}/move")
//    public void handleMove(
//            @DestinationVariable Long gameId,
//            @Payload MoveWsDto moveWsDto,
//            Principal principal
//    ) {
//        Move move = MoveMapper.createMove(moveWsDto);
//
//        gameService.makeMove(gameId, move);
//
//        gameWebSocketService.broadcastGameUpdate();
//    }
//
//}
