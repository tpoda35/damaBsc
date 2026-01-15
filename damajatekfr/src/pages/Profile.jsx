import { useEffect, useState } from "react";
import ApiService from "../services/ApiService";
import { useSharedAuth } from "../contexts/AuthContext.jsx";
import "./Profile.css";
import Loader from "../components/Loader.jsx";
import Button from "../components/Button.jsx";
import {motion} from "framer-motion";

const Profile = () => {
    const { user, fetchUser } = useSharedAuth();
    const [gameHistory, setGameHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [pageNum, setPageNum] = useState(0);
    const [pageSize] = useState(5);

    const fetchGameHistory = async () => {
        try {
            const response = await ApiService.get("/games/game-history");
            setGameHistory(response.content);
        } catch (err) {
            setError(err.message || "Failed to fetch game history");
        }
    };

    console.log('gameHistory: ', gameHistory);

    useEffect(() => {
        const loadAll = async () => {
            try {
                setLoading(true);
                await fetchUser();
                await fetchGameHistory();
            } catch (err) {
                setError(err.message || "Failed to load profile");
            } finally {
                setLoading(false);
            }
        };
        loadAll();
    }, [fetchUser]);

    const GAME_RESULT_LABELS = {
        RED_WIN: "Red won",
        BLACK_WIN: "Black won",
        RED_FORFEIT: "Red forfeited",
        BLACK_FORFEIT: "Black forfeited",
        DRAW: "Draw",
        UNDECIDED: "Undecided"
    };

    if (loading) return <Loader />;
    // if (error) return <p style={{ color: "red" }}>{error}</p>; TODO: fix this

    return (
        <motion.div
            className="profile"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 1, ease: "easeOut" }}
            style={{ willChange: "transform, opacity" }}
        >
            {user && (
                <div className="profile-card">
                    <div className="profile-default-data">
                        <p><strong>Name:</strong> {user.displayName}</p>
                        <p><strong>Email:</strong> {user.email}</p>
                        <p><strong>Created At:</strong> {new Date(user.createdAt).toLocaleString()}</p>
                        <p><strong>Updated At:</strong> {new Date(user.updatedAt).toLocaleString()}</p>
                    </div>

                    <h3>Room Stats</h3>
                    <ul className="profile-stats-list">
                        <li>Hosted Rooms: {user.hostedRoomNum}</li>
                        <li>Joined Rooms: {user.joinedRoomNum}</li>
                    </ul>

                    <h3>Vs AI Stats</h3>
                    <ul className="profile-stats-list">
                        <li>Games: {user.vsAiGames}</li>
                        <li>Wins: {user.vsAiWins}</li>
                        <li>Losses: {user.vsAiLoses}</li>
                        <li>Draws: {user.vsAiDraws}</li>
                        <li>Winrate: {user.vsBotWinrate}%</li>
                    </ul>

                    <h3>Vs Player Stats</h3>
                    <ul className="profile-stats-list">
                        <li>Games: {user.vsPlayerGames}</li>
                        <li>Wins: {user.vsPlayerWins}</li>
                        <li>Losses: {user.vsPlayerLoses}</li>
                        <li>Draws: {user.vsPlayerDraws}</li>
                        <li>Winrate: {user.vsPlayerWinrate}%</li>
                    </ul>

                    <h3>Overall</h3>
                    <ul className="profile-stats-list">
                        <li>Total Games: {user.overallGames}</li>
                        <li>Overall Winrate: {user.overallWinrate}%</li>
                    </ul>
                </div>
            )}

            <div className="profile-card">
                <h3>Game History</h3>
                {gameHistory.length === 0 ? (
                    <p className="profile-no-games">No games played yet.</p>
                ) : (
                    <>
                        <table className="profile-game-table">
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
                            {gameHistory
                                .slice(pageNum * pageSize, (pageNum + 1) * pageSize)
                                .map((game) => (
                                    <tr key={game.id}>
                                        <td>{game.redPlayer.displayName}</td>
                                        <td>{game.blackPlayer.displayName}</td>
                                        <td>{GAME_RESULT_LABELS[game.result] ?? "Unknown"}</td>
                                        <td>{game.winner ? game.winner.displayName : "---"}</td>
                                        <td>{game.totalMoves}</td>
                                        <td>
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

                        <div className="profile-pagination">
                            <Button
                                onClick={() => setPageNum((prev) => Math.max(prev - 1, 0))}
                                disabled={pageNum === 0}
                                children="Prev"
                            />
                            <span className="profile-page-info">
                                Page {pageNum + 1} of {Math.ceil(gameHistory.length / pageSize)}
                            </span>
                            <Button
                                onClick={() =>
                                    setPageNum((prev) =>
                                        Math.min(prev + 1, Math.ceil(gameHistory.length / pageSize) - 1)
                                    )
                                }
                                disabled={pageNum + 1 >= Math.ceil(gameHistory.length / pageSize)}
                                children="Next"
                            />
                        </div>
                    </>
                )}
            </div>
        </motion.div>
    );
};

export default Profile;
