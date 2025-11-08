package org.dama.damajatek.service;

import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.dto.game.websocket.IGameEvent;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.authentication.user.AppUser;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IGameService {
    Game createGame(Player redPlayer, Player blackPlayer, Room room);
    CompletableFuture<GameInfoDtoV1> getGameInfo(Long gameId);
    List<IGameEvent> makeMove(Long gameId, Move move, Principal principal);
    IGameEvent forfeit(Long gameId, PieceColor pieceColor);
}
