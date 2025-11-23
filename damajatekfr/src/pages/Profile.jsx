import { useEffect, useState } from "react";
import ApiService from "../services/ApiService";
import { useSharedAuth } from "../contexts/AuthContext.jsx";

const Profile = () => {
    const { user, fetchUser } = useSharedAuth();
    console.log(user);

    const [gameHistory, setGameHistory] = useState([]);
    console.log('GameHisotrs', gameHistory);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchGameHistory = async () => {
        try {
            const response = await ApiService.get("/games/game-history");
            setGameHistory(response.content);
        } catch (err) {
            setError(err.message || "Failed to fetch game history");
        }
    };

    useEffect(() => {
        const loadAll = async () => {
            try {
                setLoading(true);

                await fetchUser();        // loads user info dto into user
                await fetchGameHistory(); // loads history

            } catch (err) {
                setError(err.message || "Failed to load profile");
            } finally {
                setLoading(false);
            }
        };

        loadAll();
    }, [fetchUser]);

    if (loading) return <p>Loading profile...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ padding: "1rem", maxWidth: "900px", margin: "0 auto" }}>

            {/* ===================== USER PROFILE ===================== */}
            {user && (
                <div style={{ background: "#f5f5f5", padding: "1rem", borderRadius: "8px", marginBottom: "2rem" }}>
                    <h2>User Profile</h2>

                    <p><strong>ID:</strong> {user.id}</p>
                    <p><strong>Name:</strong> {user.displayName}</p>
                    <p><strong>Email:</strong> {user.email}</p>

                    <p><strong>Created At:</strong> {new Date(user.createdAt).toLocaleString()}</p>
                    <p><strong>Updated At:</strong> {new Date(user.updatedAt).toLocaleString()}</p>

                    <hr />

                    <h3>Room Stats</h3>
                    <ul>
                        <li>Hosted Rooms: {user.hostedRoomNum}</li>
                        <li>Joined Rooms: {user.joinedRoomNum}</li>
                    </ul>

                    <h3>Vs AI Stats</h3>
                    <ul>
                        <li>Games: {user.vsAiGames}</li>
                        <li>Wins: {user.vsAiWins}</li>
                        <li>Losses: {user.vsAiLoses}</li>
                        <li>Winrate: {user.vsBotWinrate}%</li>
                    </ul>

                    <h3>Vs Player Stats</h3>
                    <ul>
                        <li>Games: {user.vsPlayerGames}</li>
                        <li>Wins: {user.vsPlayerWins}</li>
                        <li>Losses: {user.vsPlayerLoses}</li>
                        <li>Winrate: {user.vsPlayerWinrate}%</li>
                    </ul>

                    <h3>Overall</h3>
                    <ul>
                        <li>Total Games: {user.overallGames}</li>
                        <li>Overall Winrate: {user.overallWinrate}%</li>
                    </ul>
                </div>
            )}

            {/* ===================== GAME HISTORY ===================== */}
            <div>
                <h2>Game History</h2>

                {gameHistory.length === 0 ? (
                    <p>No games played yet.</p>
                ) : (
                    <table>
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
                        {gameHistory.map((game) => (
                            <tr key={game.id}>
                                <td>{game.redPlayer.displayName}</td>
                                <td>{game.blackPlayer.displayName}</td>
                                <td>{game.result}</td>
                                <td>{game.winner.displayName}</td>
                                <td>{game.totalMoves}</td>
                                <td>
                                    {game.startTime && game.endTime
                                        ? `${Math.round(
                                            (new Date(game.endTime) -
                                                new Date(game.startTime)) / 1000
                                        )} sec`
                                        : "-"}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default Profile;
