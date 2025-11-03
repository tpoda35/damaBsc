package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForfeitRequest {

    private PieceColor pieceColor;

}
