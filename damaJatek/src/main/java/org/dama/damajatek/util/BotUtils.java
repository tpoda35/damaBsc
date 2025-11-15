package org.dama.damajatek.util;

import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.model.Board;

public class BotUtils {

    public static PieceColor opposite(PieceColor c) {
        return (c == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
    }

    public static int boardHash(Board board) {
        int hash = 17;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                var piece = board.getPiece(r, c);
                if (piece != null) {
                    int pieceVal = (piece.getColor().ordinal() << 16)
                            | ((piece.isKing() ? 1 : 0) << 8)
                            | (r << 4)
                            | c;
                    hash = 31 * hash + pieceVal;
                }
            }
        }
        return hash;
    }
}
