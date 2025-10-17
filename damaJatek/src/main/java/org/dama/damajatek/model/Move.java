package org.dama.damajatek.model;

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

    private List<int[]> capturedPieces = new ArrayList<>();
}

