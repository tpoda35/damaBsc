package org.dama.damajatek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.enums.game.GameResult;
import org.dama.damajatek.enums.game.GameStatus;
import org.dama.damajatek.enums.game.PieceColor;

import static org.dama.damajatek.enums.game.GameResult.UNDECIDED;
import static org.dama.damajatek.enums.game.GameStatus.IN_PROGRESS;
import static org.dama.damajatek.enums.game.PieceColor.BLACK;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue
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
    @Builder.Default
    private PieceColor currentTurn = BLACK;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GameStatus status = IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GameResult result = UNDECIDED;

    private String drawReason;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private AppUser winner;

    @Lob
    private String boardState;

    private Boolean vsBot;

    @Enumerated(EnumType.STRING)
    private BotDifficulty botDifficulty;

    @Builder.Default
    private Integer movesWithoutCaptureOrPromotion = 0;

    @Builder.Default
    private Integer totalMoves = 0;

    @Version
    private Long version;

    @PrePersist
    public void init() {
        if (currentTurn == null) currentTurn = BLACK;
        if (status == null) status = IN_PROGRESS;
        if (result == null) result = UNDECIDED;
        if (movesWithoutCaptureOrPromotion == null) movesWithoutCaptureOrPromotion = 0;
        if (totalMoves == null) totalMoves = 0;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    public void markFinished(AppUser winner, GameResult result) {
        this.status = GameStatus.FINISHED;
        this.winner = winner;
        this.result = result;
    }

    public void markFinished(AppUser winner, GameResult result, String drawReason) {
        this.winner = winner;
        this.result = result;
        this.status = GameStatus.FINISHED;
        this.drawReason = drawReason;
    }
}
