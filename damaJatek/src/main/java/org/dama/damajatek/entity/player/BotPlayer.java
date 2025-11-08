package org.dama.damajatek.entity.player;

import jakarta.persistence.*;
import lombok.*;
import org.dama.damajatek.enums.game.BotDifficulty;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue("BOT")
public class BotPlayer extends Player {

    @Enumerated(EnumType.STRING)
    private BotDifficulty difficulty;

    @Override
    public String getDisplayName() {
        return "Bot (" + difficulty.name() + ")";
    }
}
