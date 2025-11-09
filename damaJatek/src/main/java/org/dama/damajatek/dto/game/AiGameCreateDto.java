package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.enums.game.PieceColor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiGameCreateDto {

    private BotDifficulty botDifficulty;
    private PieceColor userColor; // If it's null then it's random

}
