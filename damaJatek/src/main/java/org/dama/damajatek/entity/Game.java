package org.dama.damajatek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.enums.game.GameStatus;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.authentication.user.AppUser;

import static org.dama.damajatek.enums.game.GameStatus.WAITING;
import static org.dama.damajatek.enums.game.PieceColor.BLACK;

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

    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "red_player_id")
    private AppUser redPlayer;

    @ManyToOne
    @JoinColumn(name = "black_player_id")
    private AppUser blackPlayer;

    @Enumerated(EnumType.STRING)
    private PieceColor currentTurn = BLACK;

    @Enumerated(EnumType.STRING)
    private GameStatus status = WAITING;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private AppUser winner = null;

    @Lob
    private String boardState;

    private Boolean vsBot;

    @Enumerated(EnumType.STRING)
    private BotDifficulty botDifficulty;
}

