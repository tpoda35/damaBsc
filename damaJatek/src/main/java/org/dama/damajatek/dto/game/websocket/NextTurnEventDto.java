package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Move;

import java.util.List;

import static org.dama.damajatek.enums.game.GameWsAction.NEXT_TURN;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextTurnEventDto implements GameEventDto {
    @Builder.Default
    private GameWsAction action = NEXT_TURN;

    private PieceColor currentTurn;
    private List<Move> allowedMoves;
}
