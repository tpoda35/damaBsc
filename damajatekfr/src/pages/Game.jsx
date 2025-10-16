import {useParams} from "react-router-dom";
import {useState} from "react";

const Game = () => {
    const { gameId } = useParams();
    const [game, setGame] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);


};

export default Game;