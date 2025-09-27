package org.dama.damajatek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.BotDifficulty;
import org.dama.damajatek.enums.GameStatus;
import org.dama.damajatek.enums.PieceColor;
import org.dama.damajatek.security.user.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25)
    private String name;

    // Red player (could be human or null if bot)
    @ManyToOne
    @JoinColumn(name = "red_player_id")
    private AppUser redPlayer;

    // Black player (could be human or null if bot)
    @ManyToOne
    @JoinColumn(name = "black_player_id")
    private AppUser blackPlayer;

    // Enum: RED or BLACK → whose turn it is
    @Enumerated(EnumType.STRING)
    private PieceColor currentTurn;

    // Status: WAITING, IN_PROGRESS, FINISHED
    @Enumerated(EnumType.STRING)
    private GameStatus status;

    // Winner (null if not finished)
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private AppUser winner;

    // Store board state as JSON (serialized 2D array or FEN-like string)
    @Lob
    private String boardState;

    // Flag if opponent is BOT
    private Boolean vsBot;

    // If bot → difficulty EASY, MEDIUM, HARD
    @Enumerated(EnumType.STRING)
    private BotDifficulty botDifficulty;
}

