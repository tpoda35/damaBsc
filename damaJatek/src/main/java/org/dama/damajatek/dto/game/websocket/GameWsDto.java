package org.dama.damajatek.dto.game.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.game.MoveInfoDtoV1;
import org.dama.damajatek.enums.game.GameWsAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameWsDto {

    @Enumerated(EnumType.STRING)
    @NotNull
    private GameWsAction action;

    private MoveInfoDtoV1 game;
}
