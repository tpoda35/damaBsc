package org.dama.damajatek.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.bot.IBotStrategy;
import org.dama.damajatek.dto.game.MoveResult;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.BotPlayer;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.exception.game.GameNotFoundException;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.service.IBotCacheService;
import org.dama.damajatek.service.IBotService;
import org.dama.damajatek.service.IGameWebSocketService;
import org.dama.damajatek.service.IMoveProcessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.dama.damajatek.util.BoardSerializer.loadBoard;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService implements IBotService {

    private final IGameRepository gameRepository;
    private final IGameWebSocketService gameWebSocketService;
    private final IMoveProcessor moveProcessor;
    private final IBotCacheService botCacheService;

    @Async
    @Transactional
    @Override
    public void playBotTurnAsync(Long gameId) {
        try {
            // Get the game again since out of transaction
            Game game = findGameByIdWithPlayers(gameId);
            Board board = loadBoard(game);

            List<IGameEvent> botEvents = playBotTurnIfNeeded(game, board);

            if (!botEvents.isEmpty()) {
                // Send the events out for the frontend
                for (IGameEvent event : botEvents) {
                    gameWebSocketService.broadcastGameUpdate(event, null, gameId);
                }
                log.info("Bot turn finished for game {}", gameId);
            }
        } catch (Exception e) {
            log.error("Error running bot turn async for game {}: {}", gameId, e.getMessage(), e);
        }
    }

    private List<IGameEvent> playBotTurnIfNeeded(Game game, Board board) {
        List<IGameEvent> botEvents = new ArrayList<>();

        if (game.isFinished()) {
            return botEvents;
        }

        Player currentPlayer = (game.getCurrentTurn() == PieceColor.RED)
                ? game.getRedPlayer()
                : game.getWhitePlayer();

        // Check if the Player is a bot player
        if (!(currentPlayer instanceof BotPlayer botPlayer)) {
            return botEvents;
        }

        // Get the bot with the right difficulty, then generate a move
        IBotStrategy bot = botCacheService.getOrCreateBot(game.getId(), botPlayer.getDifficulty());
        Move botMove = bot.chooseMove(board, game.getCurrentTurn());

        if (botMove == null) {
            log.warn("Bot failed to make a move in game {}", game.getId());
            return botEvents;
        }

        log.info("Bot ({}) making move in game {}", botPlayer.getDifficulty(), game.getId());

        // Make the move and add it to the events
        MoveResult result = moveProcessor.processMove(game, board, botMove);
        botEvents.addAll(result.events());

        return botEvents;
    }

    private Game findGameByIdWithPlayers(Long gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> {
                    log.info("Game not found with id {}.", gameId);
                    return new GameNotFoundException();
                });
    }
}
