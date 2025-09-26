package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

    // optional: list of captured positions (for multi-jumps)
    private List<int[]> capturedPieces = new ArrayList<>();
}

