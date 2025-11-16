package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranspositionEntry {

    int value;
    int depth;
    int flag; // 0 = exact, 1 = lowerbound, 2 = upperbound

}
