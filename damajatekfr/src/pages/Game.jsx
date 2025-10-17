import {useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import ApiService from "../services/ApiService.js";

const Game = () => {
    const { gameId } = useParams();
    const [game, setGame] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchGame = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await ApiService.get(`/games/${gameId}`);
                setGame(data);
            } catch (err) {
                setError(err.message || "Failed to fetch room info");
            } finally {
                setLoading(false);
            }
        };

        if (gameId) fetchGame();
    }, [gameId]);


};

export default Game;