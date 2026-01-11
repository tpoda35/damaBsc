import { useLocation, useNavigate } from "react-router-dom";
import styles from "./GameEnded.module.css";
import Button from "../components/Button.jsx";

export default function GameEnded() {
    const navigate = useNavigate();
    const { state } = useLocation();

    if (!state) {
        navigate("/");
        return null;
    }

    const { gameId, playerColor, winner, drawReason, draw } = state;

    const isDraw = draw === true;

    const result = winner?.result;
    const opponentColor = playerColor === "RED" ? "BLACK" : "RED";

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
            ? drawReason ?? "No winner this time"
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

                <Button
                    onClick={() => navigate("/")}
                    children="Back to Lobby"
                />
            </div>
        </div>
    );
}
