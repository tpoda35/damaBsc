import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import ApiService from "../services/ApiService";
import GameBoard from "../components/game/GameBoard.jsx";
import {useSharedWebSocket} from "../contexts/WebSocketContext.jsx";

const Game = () => {
    const { gameId } = useParams();
    const [game, setGame] = useState(null);
    const [selectedCell, setSelectedCell] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const { isConnected, subscribe, sendMessage } = useSharedWebSocket();

    const fetchGame = async () => {
        try {
            setLoading(true);
            const data = await ApiService.get(`/games/${gameId}`);
            setGame(data);
        } catch (err) {
            setError(err.message || "Failed to fetch game info");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (gameId) fetchGame();
    }, [gameId]);

    useEffect(() => {
        if (!isConnected || gameId == null) return;

        const destination = `/topic/games/${gameId}/move`;

        const unsub = subscribe(
            destination,
            (message) => {
                try {
                    const response = JSON.parse(message.body);
                    console.log("Incoming game message:", response);

                    const { type, noteId, content } = response;

                    setTripNotes((prevNotes = []) => {
                        let updatedNotes = prevNotes;

                        switch (type) {
                            case "NOTE_CREATED":
                                if (prevNotes.some((note) => Number(note.id) === noteIdNumber)) {
                                    return prevNotes;
                                }
                                updatedNotes = [...prevNotes, { id: noteIdNumber, content }];
                                break;

                            case "NOTE_UPDATED":
                                updatedNotes = prevNotes.map((note) =>
                                    Number(note.id) === noteIdNumber ? { ...note, content } : note
                                );
                                break;

                            case "NOTE_DELETED":
                                updatedNotes = prevNotes.filter((note) => Number(note.id) !== noteIdNumber);
                                break;

                            default:
                                return prevNotes;
                        }

                        return updatedNotes;
                    });
                } catch (e) {
                    console.error("Error parsing game message:", e);
                }
            }, [gameId]);

    const handleCellClick = (row, col) => {
        const cellPiece = game.board.grid[row][col];
        if (
            selectedCell &&
            selectedCell.row === row &&
            selectedCell.col === col
        ) {
            setSelectedCell(null);
        } else if (cellPiece) {
            setSelectedCell({ row, col });
        }
    };

    if (loading) return <p className="text-center mt-6">Loading game...</p>;
    if (error) return <p className="text-red-500 text-center mt-6">{error}</p>;
    if (!game) return null;

    return (
        <div className="flex flex-col items-center mt-6">
            <h2 className="text-xl font-semibold mb-2">
                Game #{game.id} — You are{" "}
                <span className="capitalize">{game.playerColor.toLowerCase()}</span>
            </h2>
            <p className="mb-4">
                Current turn:{" "}
                <span className="capitalize">{game.currentTurn.toLowerCase()}</span>
            </p>

            <GameBoard
                board={game.board}
                allowedMoves={game.allowedMoves}
                selectedCell={selectedCell}
                onCellClick={handleCellClick}
                playerColor={game.playerColor}
            />
        </div>
    );
};

export default Game;
