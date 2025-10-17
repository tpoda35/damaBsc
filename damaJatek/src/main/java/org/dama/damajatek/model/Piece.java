package org.dama.damajatek.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Piece {
    private PieceColor color;
    private boolean isKing;
}
