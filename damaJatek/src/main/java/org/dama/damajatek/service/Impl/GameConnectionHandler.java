package org.dama.damajatek.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.dto.game.websocket.disconnect.DisconnectDto;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.HumanPlayer;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.game.GameAlreadyFinishedException;
import org.dama.damajatek.exception.game.GameNotFoundException;
import org.dama.damajatek.mapper.EventMapper;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.service.IGameConnectionHandler;
import org.dama.damajatek.service.IGameScheduler;
import org.springframework.stereotype.Component;

import static org.dama.damajatek.enums.game.GameResult.DRAW;

@Component
@RequiredArgsConstructor
public class GameConnectionHandler implements IGameConnectionHandler {

    private final IGameRepository gameRepository;
    private final IGameScheduler gameScheduler;

    @Transactional
    @Override
    public DisconnectDto handleDisconnect(String email) {
        Game game = gameRepository.findInProgressGameByUserEmail(email)
                .orElseThrow(GameNotFoundException::new);

        if (game.isFinished()) throw new GameAlreadyFinishedException();

        IGameEvent event = null;

        boolean isRed = game.getRedPlayer() instanceof HumanPlayer redHuman &&
                redHuman.getUser().getEmail().equals(email);

        boolean isWhite = game.getWhitePlayer() instanceof HumanPlayer whiteHuman &&
                whiteHuman.getUser().getEmail().equals(email);

        if (!isRed && !isWhite) throw new AccessDeniedException("You are not a participant in this game");

        if (isRed) {
            if (Boolean.TRUE.equals(game.getWhiteDisconnected())) {
                game.markFinished(null, DRAW, "Opponent disconnected");
                event = EventMapper.createGameDrawEvent("Opponent disconnected");
            } else {
                game.setRedDisconnected(true);

                gameScheduler.scheduleTimeout(email, game.getId());
            }
        } else {
            if (Boolean.TRUE.equals(game.getRedDisconnected())) {
                game.markFinished(null, DRAW, "Opponent disconnected");
                event = EventMapper.createGameDrawEvent("Opponent disconnected");
            } else {
                game.setWhiteDisconnected(true);

                gameScheduler.scheduleTimeout(email, game.getId());
            }
        }

        gameRepository.save(game);

        return DisconnectDto.builder()
                .gameEvent(event)
                .gameId(game.getId())
                .build();
    }

    @Override
    public void handleReconnect(String email) {
        Game game = gameRepository.findInProgressGameByUserEmail(email)
                .orElseThrow(GameNotFoundException::new);

        if (game.isFinished()) throw new GameAlreadyFinishedException();

        boolean isRed = game.getRedPlayer() instanceof HumanPlayer redHuman &&
                redHuman.getUser().getEmail().equals(email);

        boolean isWhite = game.getWhitePlayer() instanceof HumanPlayer whiteHuman &&
                whiteHuman.getUser().getEmail().equals(email);

        if (!isRed && !isWhite) throw new AccessDeniedException("You are not a participant in this game");

        if (isRed) {
            game.setRedDisconnected(false);
        } else {
            game.setWhiteDisconnected(false);
        }
    }

}
