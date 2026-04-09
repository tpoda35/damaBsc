package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InGameDto {

    private Boolean isInGame;
    private Long gameId;

}
