import {useEffect, useState} from "react";
import ApiService from "../services/ApiService";
import {useSharedAuth} from "../contexts/AuthContext.jsx";
import styles from "./Profile.module.css";
import AnimatedPage from "../components/AnimatedPage.jsx";
import {toast} from "react-toastify";
import Pagination from "../components/Pagination.jsx";

const Profile = () => {
    const { user, fetchUser } = useSharedAuth();
    const [gameHistoryPage, setGameHistoryPage] = useState([]);
    const [pageNum, setPageNum] = useState(0);
    const [pageSize] = useState(5);

    const fetchGameHistory = async (page = pageNum, size = pageSize) => {
        const response = await ApiService.get(`/games/game-history?pageNum=${page}&pageSize=${size}`);
        setGameHistoryPage(response);
    };

    useEffect(() => {
        const loadGameHistory = async () => {
            try {
                await fetchGameHistory(pageNum);
            } catch {
                toast.error("Failed to load game history");
            }
        };

        loadGameHistory();
    }, [pageNum, pageSize]);

    useEffect(() => {
        const loadAll = async () => {
            try {
                await fetchUser();
                await fetchGameHistory();
            } catch {
                // I didn't used the withToastError.js here, because I dont want to return the api error response
                toast.error("Failed to load profile");
            }
        };

        loadAll();
    }, [fetchUser]);

    const GAME_RESULT_LABELS = {
        RED_WIN: "Red won",
        WHITE_WIN: "White won",
        RED_FORFEIT: "Red forfeited",
        WHITE_FORFEIT: "White forfeited",
        DRAW: "Draw",
        UNDECIDED: "Undecided"
    };

    const gameHistoryList = gameHistoryPage?.content || [];
    const totalPages = gameHistoryPage?.totalPages || 1;

    console.log(gameHistoryList);

    return (
        <AnimatedPage className={styles.profile}>
            {user && (
                <div className={styles.profileCard}>
                    <div className={styles.profileDefaultData}>
                        <p><strong>Name:</strong><br /> {user.displayName}</p>
                        <p><strong>Email:</strong><br /> {user.email}</p>
                        <p><strong>Created At:</strong><br /> {new Date(user.createdAt).toLocaleString()}</p>
                        <p><strong>Updated At:</strong><br /> {new Date(user.updatedAt).toLocaleString()}</p>
                    </div>

                    <h3>Room Stats</h3>
                    <ul className={styles.profileStatsList}>
                        <li>Hosted Rooms: {user.hostedRoomNum}</li>
                        <li>Joined Rooms: {user.joinedRoomNum}</li>
                    </ul>

                    <h3>Vs AI Stats</h3>
                    <ul className={styles.profileStatsList}>
                        <li>Games: {user.vsAiGames}</li>
                        <li>Wins: {user.vsAiWins}</li>
                        <li>Losses: {user.vsAiLoses}</li>
                        <li>Draws: {user.vsAiDraws}</li>
                        <li>Winrate: {user.vsBotWinrate}%</li>
                    </ul>

                    <h3>Vs Player Stats</h3>
                    <ul className={styles.profileStatsList}>
                        <li>Games: {user.vsPlayerGames}</li>
                        <li>Wins: {user.vsPlayerWins}</li>
                        <li>Losses: {user.vsPlayerLoses}</li>
                        <li>Draws: {user.vsPlayerDraws}</li>
                        <li>Winrate: {user.vsPlayerWinrate}%</li>
                    </ul>

                    <h3>Overall</h3>
                    <ul className={styles.profileStatsList}>
                        <li>Total Games: {user.overallGames}</li>
                        <li>Overall Winrate: {user.overallWinrate}%</li>
                    </ul>
                </div>
            )}

            <div className={styles.profileCard}>
                <h3>Game History</h3>
                {gameHistoryPage.length === 0 ? (
                    <p className={styles.profileNoGames}>No games played yet.</p>
                ) : (
                    <>
                        <div className={styles.tableWrapper}>
                            <table className={styles.profileGameTable}>
                                <thead>
                                <tr>
                                    <th>Red Player</th>
                                    <th>Black Player</th>
                                    <th>Result</th>
                                    <th>Winner</th>
                                    <th>Total Moves</th>
                                    <th>Game Time</th>
                                </tr>
                                </thead>
                                <tbody>
                                {gameHistoryList.map((game) => (
                                    <tr key={game.id}>
                                        <td data-label="Red Player">
                                            {game.redPlayer.displayName}
                                        </td>

                                        <td data-label="Black Player">
                                            {game.whitePlayer.displayName}
                                        </td>

                                        <td data-label="Result">
                                            {GAME_RESULT_LABELS[game.result] ?? "Unknown"}
                                        </td>

                                        <td data-label="Winner">
                                            {game.winner ? game.winner.displayName : "---"}
                                        </td>

                                        <td data-label="Total Moves">
                                            {game.totalMoves}
                                        </td>

                                        <td data-label="Game Time">
                                            {game.startTime && game.endTime
                                                ? (() => {
                                                    const totalSeconds = Math.round(
                                                        (new Date(game.endTime) - new Date(game.startTime)) / 1000
                                                    );
                                                    const minutes = Math.floor(totalSeconds / 60);
                                                    const seconds = totalSeconds % 60;

                                                    return minutes > 0
                                                        ? `${minutes} min ${seconds} sec`
                                                        : `${seconds} sec`;
                                                })()
                                                : "-"}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <Pagination
                            pageNum={pageNum}
                            totalPages={totalPages}
                            onPageChange={setPageNum}
                        />
                    </>
                )}
            </div>
        </AnimatedPage>
    );
};

export default Profile;
