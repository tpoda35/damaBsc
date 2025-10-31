package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;
import org.dama.damajatek.enums.game.PieceColor;

import static org.dama.damajatek.enums.game.GameWsAction.PROMOTED_PIECE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotedPieceEventDto implements GameEventDto {

    @Builder.Default
    private GameWsAction action = PROMOTED_PIECE;

    private int row;
    private int col;
    private PieceColor pieceColor;

}
