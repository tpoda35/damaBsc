import { useLocation, useNavigate } from "react-router-dom";
import styles from "./GameEnded.module.css";

export default function GameEnded() {
    const navigate = useNavigate();
    const { state } = useLocation();
    console.log('State: ', state);

    if (!state) {
        navigate("/");
        return null;
    }

    const { gameId, playerColor, winner } = state;

    const result = winner?.result;
    const opponentColor = playerColor === "RED" ? "BLACK" : "RED";

    const isDraw = result === "DRAW";

    const playerWon =
        result === `${playerColor}_WIN` ||
        result === `${opponentColor}_FORFEIT`;

    const playerForfeited =
        result === `${playerColor}_FORFEIT`;

    const title = isDraw
        ? "Game Draw"
        : playerWon
            ? "You Win"
            : "You Lose";

    const subtitle = playerForfeited
        ? "You forfeited the game"
        : isDraw
            ? "No winner this time"
            : `Winner: ${winner.name}`;

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h1 className={styles.title}>{title}</h1>

                <p className={styles.subtitle}>
                    Game #{gameId}
                </p>

                <p className={styles.result}>
                    {subtitle}
                </p>

                <div className={styles.actions}>
                    <button onClick={() => navigate("/")}>
                        Back to Lobby
                    </button>

                    <button
                        className={styles.primary}
                        onClick={() => navigate("/new-game")}
                    >
                        New Game
                    </button>
                </div>
            </div>
        </div>
    );
}
