import {useParams} from "react-router-dom";
import {useCallback, useEffect, useState} from "react";
import ApiService from "../services/ApiService";
import GameBoard from "../components/game/GameBoard.jsx";
import {useSharedWebSocket} from "../contexts/WebSocketContext.jsx";

const Game = () => {
    const { gameId } = useParams();
    const [game, setGame] = useState(null);
    console.log(game);
    const [selectedCell, setSelectedCell] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

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
                                const { move } = response;
                                console.log("MOVE_MADE response: ", response);
                                updatedGame.board.grid[move.toRow][move.toCol] =
                                    updatedGame.board.grid[move.fromRow][move.fromCol];
                                updatedGame.board.grid[move.fromRow][move.fromCol] = null;
                                break;
                            }

                            case "CAPTURE_MADE": {
                                const { move } = response;
                                console.log("CAPTURE_MADE response: ", response);

                                // Move the capturing piece
                                updatedGame.board.grid[move.toRow][move.toCol] =
                                    updatedGame.board.grid[move.fromRow][move.fromCol];
                                updatedGame.board.grid[move.fromRow][move.fromCol] = null;

                                // Remove all captured pieces
                                if (move.capturedPieces && move.capturedPieces.length > 0) {
                                    move.capturedPieces.forEach(([row, col]) => {
                                        updatedGame.board.grid[row][col] = null;
                                    });
                                }

                                break;
                            }

                            case "PROMOTED_PIECE": {
                                const { row, col, pieceColor } = response;
                                console.log(`PROMOTED_PIECE: (${row}, ${col}) for ${pieceColor}`);

                                const piece = updatedGame.board.grid[row][col];
                                if (piece && piece.color === pieceColor) {
                                    updatedGame.board.grid[row][col] = {
                                        ...piece,
                                        king: true
                                    };
                                }
                                break;
                            }

                            case "NEXT_TURN": {
                                console.log("NEXT_TURN response: ", response);
                                updatedGame.currentTurn = response.currentTurn;
                                updatedGame.allowedMoves = response.allowedMoves || [];
                                setSelectedCell(null);
                                break;
                            }

                            case "GAME_OVER": {
                                const { winnerName, winnerColor } = response;
                                console.log(`Game over! Winner: ${winnerName} (${winnerColor})`);

                                updatedGame.currentTurn = null;
                                updatedGame.allowedMoves = [];

                                updatedGame.winner = { name: winnerName, color: winnerColor };

                                setTimeout(() => {
                                    alert(`🏁 Game Over!\nWinner: ${winnerName} (${winnerColor})`);
                                }, 100);

                                break;
                            }

                            case "GAME_DRAW": {
                                const { drawReason } = response;

                                updatedGame.currentTurn = null;
                                updatedGame.allowedMoves = [];
                                updatedGame.winner = null;

                                setTimeout(() => {
                                    alert(`🤝 Game Draw!\nReason: ${drawReason}`);
                                }, 100);

                                break;
                            }

                            case "GAME_FORFEIT": {
                                const { winnerName, gameResult, message } = response;
                                console.log(`Game forfeited! Winner: ${winnerName}, Result: ${gameResult}`);

                                // Stop the game — disable moves
                                updatedGame.currentTurn = null;
                                updatedGame.allowedMoves = [];
                                updatedGame.winner = { name: winnerName, result: gameResult };

                                setTimeout(() => {
                                    alert(`🏳️ Game Forfeit!\n${message || `${winnerName} wins by forfeit.`}`);
                                }, 100);

                                break;
                            }

                            case "INVALID_MOVE": {
                                console.warn("Invalid move detected");
                                setSelectedCell(null);
                                break;
                            }

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

    const handleForfeit = useCallback(async () => {
        if (!game) return;

        try {
            await ApiService.post(`/games/${gameId}/forfeit`, {
                pieceColor: game.playerColor,
            });
        } catch {
            alert("Failed to forfeit the game. Please try again.");
        }
    }, [game, gameId]);

    if (loading) return <p>Loading game...</p>;
    if (error) return <p>{error}</p>;
    if (!game) return null;

    return (
        <div>
            <h2>
                Game #{game.id} — You are{" "}
                <span>{game.playerColor.toLowerCase()}</span>
            </h2>
            <p>
                Current turn:{" "}
                <span>{game.currentTurn ? game.currentTurn.toLowerCase() : "—"}</span>
            </p>

            <button
                onClick={handleForfeit}
                disabled={!game.currentTurn || game.winner}
            >
                Forfeit
            </button>

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