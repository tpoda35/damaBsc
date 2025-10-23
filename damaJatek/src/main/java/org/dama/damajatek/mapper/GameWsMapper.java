package org.dama.damajatek.mapper;

import org.dama.damajatek.dto.game.websocket.GameWsDto;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.game.GameWsAction;
import org.dama.damajatek.model.Move;

import java.util.List;

public class GameWsMapper {

    public static GameWsDto createGameWsDto(GameWsAction action, Game game, List<Move> allowedMoves) {
        return GameWsDto.builder()
                .action(action)
                .game(
                    GameMapper.createGameInfoDtoV2(game, allowedMoves)
                )
                .build();
    }

}
