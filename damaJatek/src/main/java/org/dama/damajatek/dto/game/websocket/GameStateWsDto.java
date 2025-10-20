package org.dama.damajatek.dto.game.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.enums.game.GameStateWsAction;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameStateWsDto {

    @Enumerated(EnumType.STRING)
    @NotNull
    private GameStateWsAction action;

    private GameInfoDtoV1 game;
}
