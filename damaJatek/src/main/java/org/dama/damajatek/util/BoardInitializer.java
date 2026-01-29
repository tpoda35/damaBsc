package org.dama.damajatek.util;

import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Piece;

import static org.dama.damajatek.enums.game.PieceColor.RED;
import static org.dama.damajatek.enums.game.PieceColor.WHITE;

public class BoardInitializer {

    public static Board createStartingBoard() {
        Board board = new Board(new Piece[8][8]);

        // Place red pieces (top rows 0–2)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board.setPiece(row, col, new Piece(RED, false));
                }
            }
        }

        // Place black pieces (bottom rows 5–7)
        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board.setPiece(row, col, new Piece(WHITE, false));
                }
            }
        }

        return board;
    }

}
