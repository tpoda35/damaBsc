package org.dama.damajatek.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.PieceColor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Piece {
    private PieceColor color;  // RED or BLACK
    private boolean isKing;
}
