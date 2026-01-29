package org.dama.damajatek.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.GameStatus;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameHistoryDto {

    private Long id;

    private Player redPlayer;
    private Player whitePlayer;

    private GameStatus status;
    private GameResult result;

    private String drawReason;

    private Player winner;

    private Integer totalMoves;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private OffsetDateTime gameTime;

}
