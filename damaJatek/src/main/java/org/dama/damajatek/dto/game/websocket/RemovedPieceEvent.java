package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;
import org.dama.damajatek.enums.game.PieceColor;

import static org.dama.damajatek.enums.game.GameWsAction.REMOVED_PIECE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemovedPieceEvent implements IGameEvent {

    @Builder.Default
    private GameWsAction action = REMOVED_PIECE;

    private PieceColor pieceColor;
}
