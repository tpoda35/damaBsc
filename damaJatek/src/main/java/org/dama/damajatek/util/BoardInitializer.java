package org.dama.damajatek.util;

import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.enums.game.PieceColor;

public class BoardInitializer {

    public static Board createStartingBoard() {
        Board board = new Board(new Piece[8][8]);

        // Place red pieces (top rows 0–2)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board.setPiece(row, col, new Piece(PieceColor.RED, false));
                }
            }
        }

        // Place black pieces (bottom rows 5–7)
        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board.setPiece(row, col, new Piece(PieceColor.WHITE, false));
                }
            }
        }

        return board;
    }

}
