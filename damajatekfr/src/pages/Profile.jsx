import { useEffect, useState } from "react";
import ApiService from "../services/ApiService";

const Profile = () => {
    const [gameHistory, setGameHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchGameHistory = async () => {
        try {
            setLoading(true);
            const response = await ApiService.get("/profiles/game-history");
            console.log(response);
            setGameHistory(response.content);
        } catch (err) {
            setError(err.message || "Failed to fetch game history");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchGameHistory();
    }, []);

    if (loading) return <p>Loading game history...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
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
                            <td>{game.totalMoves}</td>
                            <td>
                                {game.startTime && game.endTime
                                    ? Math.round(
                                    (new Date(game.endTime) - new Date(game.startTime)) / 1000
                                ) + " sec"
                                    : "-"}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default Profile;
