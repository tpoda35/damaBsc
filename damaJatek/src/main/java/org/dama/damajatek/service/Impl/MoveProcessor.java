package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.authentication.user.IAppUserCacheService;
import org.dama.damajatek.dto.game.MoveResult;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.mapper.EventMapper;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.service.IGameEngine;
import org.dama.damajatek.service.IMoveProcessor;
import org.dama.damajatek.util.BoardSerializer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MoveProcessor implements IMoveProcessor {

    private final IGameEngine gameEngine;
    private final IGameRepository gameRepository;
    private final IAppUserCacheService appUserCacheService;

    // There's no validation, so the full request needs to be validated before this
    public MoveResult processMove(Game game, Board board, Move move) {
        List<IGameEvent> events = new ArrayList<>();

        // Apply move and check promotion
        gameEngine.applyMove(board, move);
        boolean wasPromoted = gameEngine.promoteIfKing(board, move);

        // Update counters
        game.setTotalMoves(game.getTotalMoves() + 1);
        if (!move.getCapturedPieces().isEmpty() || wasPromoted) {
            game.setMovesWithoutCaptureOrPromotion(0);
        } else {
            game.setMovesWithoutCaptureOrPromotion(game.getMovesWithoutCaptureOrPromotion() + 1);
        }

        // Add events
        if (!move.getCapturedPieces().isEmpty()) {
            events.add(EventMapper.createCaptureMadeEvent(move));
        } else {
            events.add(EventMapper.createMoveMadeEvent(move));
        }

        if (wasPromoted) {
            events.add(EventMapper.createPromotedPieceEvent(
                    move,
                    board.getPiece(move.getToRow(), move.getToCol()).getColor()
            ));
        }

        // Check game over
        PieceColor nextTurn = (game.getCurrentTurn() == PieceColor.RED)
                ? PieceColor.WHITE
                : PieceColor.RED;

        boolean gameOver = gameEngine.isGameOver(board, nextTurn, game);

        if (gameOver) {
            if (game.getResult() == GameResult.DRAW) {
                events.add(EventMapper.createGameDrawEvent(game.getDrawReason()));
            } else if (game.getWinner() != null) {
                PieceColor winnerColor = getWinnerColor(game);

                events.add(EventMapper.createGameOverEvent(
                        game.getWinner().getDisplayName(),
                        winnerColor,
                        game.getResult()
                ));
            } else {
                events.add(EventMapper.createGameDrawEvent());
            }

            appUserCacheService.evictPlayers(game);
        } else {
            game.setCurrentTurn(nextTurn);
            events.add(EventMapper.createNextTurnEvent(
                    nextTurn,
                    gameEngine.getAvailableMoves(board, nextTurn)
            ));
        }

        BoardSerializer.saveBoard(game, board);
        gameRepository.save(game);

        return new MoveResult(events, nextTurn, gameOver);
    }

    private PieceColor getWinnerColor(Game game) {
        Player winner = game.getWinner();
        if (winner == null) {
            return null;
        }

        Long winnerId = winner.getId();

        if (winnerId.equals(game.getRedPlayer().getId())) {
            return PieceColor.RED;
        }

        if (winnerId.equals(game.getBlackPlayer().getId())) {
            return PieceColor.WHITE;
        }

        throw new IllegalStateException("Winner is not part of the game");
    }
}
