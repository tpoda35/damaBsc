package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameInfoDto {

    private Long id;
    private Board board;
    private PieceColor currentTurn;
    private List<Move> allowedMoves;

    private Integer removedRedPieces;
    private Integer removedWhitePieces;

    private PieceColor playerColor;

}
