package org.dama.damajatek.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

    @Builder.Default
    private List<int[]> path = new ArrayList<>();

    @Builder.Default
    private List<int[]> capturedPieces = new ArrayList<>();
}

