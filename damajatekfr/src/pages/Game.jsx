import {useParams} from "react-router-dom";
import {useCallback, useEffect, useRef, useState} from "react";
import ApiService from "../services/ApiService";
import GameBoard from "../components/game/GameBoard.jsx";
import {useSharedWebSocket} from "../contexts/WebSocketContext.jsx";

const Game = () => {
    const { gameId } = useParams();
    const [game, setGame] = useState(null);
    console.log("Game: ", game);
    const [selectedCell, setSelectedCell] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // This is needed because of useState async behavior.
    const gameRef = useRef(null);

    useEffect(() => {
        gameRef.current = game;
    }, [game]);

    const { isConnected, subscribe, sendMessage } = useSharedWebSocket();

    const fetchGame = useCallback(async () => {
        if (!gameId) return;

        setLoading(true);
        try {
            const data = await ApiService.get(`/games/${gameId}`);
            setGame(data);
        } catch (err) {
            setError(err.message || "Failed to fetch game info");
        } finally {
            setLoading(false);
        }
    }, [gameId]);

    useEffect(() => {
        fetchGame();
    }, [fetchGame]);

    useEffect(() => {
        if (!isConnected || gameId == null) return;

        const destination = `/topic/games/${gameId}`;

        const unsub = subscribe(
            destination,
            (message) => {
                try {
                    const response = JSON.parse(message.body);
                    const { action } = response;
                    console.log("Response: ", response);

                    setGame((prevGame) => {
                        if (!prevGame) return prevGame;

                        let updatedGame = {
                            ...prevGame,
                            board: {
                                ...prevGame.board,
                                grid: prevGame.board.grid.map(row => [...row])
                            },
                            allowedMoves: [...(prevGame.allowedMoves || [])],
                            currentTurn: prevGame.currentTurn
                        };

                        switch (action) {
                            case "MOVE_MADE": {
                                const { fromRow, fromCol, toRow, toCol } = response;
                                updatedGame.board.grid[toRow][toCol] =
                                    updatedGame.board.grid[fromRow][fromCol];
                                updatedGame.board.grid[fromRow][fromCol] = null;
                                break;
                            }

                            case "CAPTURE_MADE": {
                                const { fromRow, fromCol, toRow, toCol, capturedPieces } = response;
                                console.log("CAPTURE_MADE response: ", response);

                                // Move the capturing piece
                                updatedGame.board.grid[toRow][toCol] =
                                    updatedGame.board.grid[fromRow][fromCol];
                                updatedGame.board.grid[fromRow][fromCol] = null;

                                // Remove all captured pieces
                                if (capturedPieces && capturedPieces.length > 0) {
                                    capturedPieces.forEach(([row, col]) => {
                                        updatedGame.board.grid[row][col] = null;
                                    });
                                }

                                break;
                            }

                            case "NEXT_TURN": {
                                updatedGame.currentTurn = response.currentTurn;
                                updatedGame.allowedMoves = response.allowedMoves || [];
                                setSelectedCell(null);
                                break;
                            }

                            case "INVALID_MOVE": {
                                console.warn("Invalid move detected");
                                setSelectedCell(null);
                                break;
                            }

                            case "GAME_OVER":
                            case "FORFEIT":
                                break;

                            default:
                                return prevGame;
                        }

                        return updatedGame;
                    });
                } catch (e) {
                    console.error("Error parsing game message:", e);
                }
            },
            [gameId]
        );

        return () => unsub();
    }, [isConnected, gameId, subscribe]);

    const handleCellClick = (row, col) => {
        if (!game) {
            console.log("No game state available");
            return;
        }

        const cellPiece = game.board.grid[row][col];

        if (!selectedCell && cellPiece && cellPiece.color === game.playerColor) {
            setSelectedCell({ row, col });
            return;
        }

        if (selectedCell) {
            if (game.currentTurn !== game.playerColor) {
                setSelectedCell(null);
                return;
            }

            const isMoveAllowed = game.allowedMoves.some(
                (move) => {
                    return move.fromRow === selectedCell.row &&
                        move.fromCol === selectedCell.col &&
                        move.toRow === row &&
                        move.toCol === col;
                }
            );

            if (!isMoveAllowed) {
                setSelectedCell(null);
                return;
            }

            // Prepare MoveDto
            const moveDto = {
                fromRow: selectedCell.row,
                fromCol: selectedCell.col,
                toRow: row,
                toCol: col,
            };

            const destination = `/app/games/${gameId}/move`;
            sendMessage(destination, JSON.stringify(moveDto));

            setSelectedCell(null);
        }
    };

    if (loading) return <p>Loading game...</p>;
    if (error) return <p>{error}</p>;
    if (!game) return null;

    return (
        <div>
            <h2>
                Game #{game.id} — You are{" "}
                <span>{game.playerColor.toLowerCase()}</span>
            </h2>
            <p className="mb-4">
                Current turn:{" "}
                <span>{game.currentTurn.toLowerCase()}</span>
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