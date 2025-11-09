package org.dama.damajatek.dto.game;

import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.enums.game.PieceColor;

import java.util.List;

public record MoveResult(List<IGameEvent> events, PieceColor nextTurn, boolean gameOver) {
}
