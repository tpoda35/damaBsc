import {useNavigate, useParams} from "react-router-dom";
import {useCallback, useEffect, useState} from "react";
import ApiService from "../services/ApiService";
import GameBoard from "../components/game/GameBoard.jsx";
import {useSharedWebSocket} from "../contexts/WebSocketContext.jsx";
import Loader from "../components/Loader.jsx";
import {toast} from "react-toastify";

import styles from './Game.module.css';
import Button from "../components/Button.jsx";
import GameIntro from "../components/GameIntro.jsx";
import {useSharedAuth} from "../contexts/AuthContext.jsx";

const Game = () => {
    const { gameId } = useParams();
    const [game, setGame] = useState(null);
    const [selectedCell, setSelectedCell] = useState(null);

    const [loading, setLoading] = useState(true);

    // Removed piece counter
    const [removedRedPieces, setRemovedRedPieces] = useState(0);
    const [removedWhitePieces, setRemovedWhitePieces] = useState(0);

    const [showIntro, setShowIntro] = useState(true);
    const { user } = useSharedAuth();

    const navigate = useNavigate();

    const { isConnected, subscribe, sendMessage } = useSharedWebSocket();

    const initializePieceIds = (board) => {
        let redCount = 0;
        let blackCount = 0;

        return {
            ...board,
            grid: board.grid.map(row =>
                row.map(piece => {
                    if (!piece) return null;
                    if (piece.id) return piece;

                    const count = piece.color.toLowerCase() === 'red' ? redCount++ : blackCount++;
                    return {
                        ...piece,
                        id: `${piece.color.toLowerCase()}-${count}`
                    };
                })
            )
        };
    };

    useEffect(() => {
        if (game?.winner || game?.draw) {
            navigate("/game-ended", {
                state: {
                    winner: game.winner,
                    playerColor: game.playerColor,
                    gameId: game.id,
                    drawReason: game.drawReason,
                    draw: game.draw
                },
            });
        }
    }, [game?.id, game?.winner, game?.draw, navigate]);

    const fetchGame = useCallback(async () => {
        if (!gameId) return;

        setLoading(true);
        try {
            const data = await ApiService.get(`/games/${gameId}`);
            setGame({
                ...data,
                board: initializePieceIds(data.board)
            });

            setRemovedRedPieces(data.removedRedPieces ?? 0);
            setRemovedWhitePieces(data.removedWhitePieces ?? 0);
        } catch (err) {
            toast.error(err.message || "Failed to fetch game info");
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

                                updatedGame.board.grid[move.toRow][move.toCol] = updatedGame.board.grid[move.fromRow][move.fromCol];
                                updatedGame.board.grid[move.fromRow][move.fromCol] = null;
                                break;
                            }

                            case "CAPTURE_MADE": {
                                const { move } = response;

                                // Get the moving piece
                                const movingPiece = updatedGame.board.grid[move.fromRow][move.fromCol];

                                if (!movingPiece) {
                                    console.error("No piece found at capture start position");
                                    return prevGame;
                                }

                                // Store animation data
                                const animationId = `capture-${Date.now()}`;

                                // Don't remove captured pieces immediately, remove them during animation
                                // Store captured pieces for removal during animation
                                const capturedPositions = move.capturedPieces || [];

                                // Remove the piece from the starting pos
                                updatedGame.board.grid[move.fromRow][move.fromCol] = null;

                                const animationPath = [];

                                // Add all intermediate jumps from move.path
                                if (move.path && move.path.length > 0) {
                                    move.path.forEach(([row, col], index) => {
                                        animationPath.push({
                                            row,
                                            col,
                                            duration: 1000,
                                            pause: index < move.path.length - 1 ? 500 : 0,
                                            removeCapturedAtThisStep: index
                                        });
                                    });
                                }

                                // Add final dest
                                animationPath.push({
                                    row: move.toRow,
                                    col: move.toCol,
                                    duration: 1000,
                                    pause: 0,
                                    removeCapturedAtThisStep: move.path ? move.path.length : 0
                                });

                                // Create animation state
                                updatedGame.animation = {
                                    type: 'CAPTURE',
                                    pieceId: movingPiece.id,
                                    animationId: animationId,
                                    path: animationPath,
                                    currentStep: 0,
                                    piece: movingPiece,
                                    finalPosition: {
                                        row: move.toRow,
                                        col: move.toCol,
                                        piece: movingPiece
                                    },
                                    capturedPositions: capturedPositions,
                                    removedCapturedPieces: [],
                                    pendingPromotion: null  // Will be set by PROMOTED_PIECE if needed
                                };

                                const animateStep = (stepIndex) => {
                                    // If there's no more jump left, set the final dest
                                    if (stepIndex >= animationPath.length) {
                                        setTimeout(() => {
                                            setGame(prev => {
                                                if (!prev || !prev.animation || prev.animation.animationId !== animationId) {
                                                    return prev;
                                                }

                                                const newGrid = prev.board.grid.map(row => [...row]);
                                                const { row: finalRow, col: finalCol, piece } = prev.animation.finalPosition;

                                                prev.animation.capturedPositions.forEach(([r, c]) => {
                                                    newGrid[r][c] = null;
                                                });

                                                // Apply promotion here if it was flagged during animation, in the same state update
                                                let finalPiece = piece;
                                                const pending = prev.animation.pendingPromotion;
                                                if (pending && pending.row === finalRow && pending.col === finalCol) {
                                                    finalPiece = { ...piece, king: true };
                                                }

                                                newGrid[finalRow][finalCol] = finalPiece;

                                                return {
                                                    ...prev,
                                                    board: { ...prev.board, grid: newGrid },
                                                    animation: null
                                                };
                                            });
                                        }, 300);

                                        return;
                                    }

                                    setTimeout(() => {
                                        setGame(prev => {
                                            if (!prev || !prev.animation || prev.animation.animationId !== animationId) {
                                                return prev;
                                            }

                                            const newAnimation = {
                                                ...prev.animation,
                                                currentStep: stepIndex
                                            };

                                            // Temporarily place piece at current step position for Framer Motion
                                            const newGrid = prev.board.grid.map(row => [...row]);
                                            const currentPos = animationPath[stepIndex];
                                            newGrid[currentPos.row][currentPos.col] = prev.animation.piece;

                                            // Remove from previous position
                                            if (stepIndex > 0) {
                                                const prevPos = animationPath[stepIndex - 1];
                                                newGrid[prevPos.row][prevPos.col] = null;
                                            }

                                            const removeIndex = currentPos.removeCapturedAtThisStep;
                                            if (removeIndex >= 0 && removeIndex < prev.animation.capturedPositions.length) {
                                                const [r, c] = prev.animation.capturedPositions[removeIndex];
                                                newGrid[r][c] = null;
                                                newAnimation.removedCapturedPieces = [...prev.animation.removedCapturedPieces, [r, c]];
                                            }

                                            return {
                                                ...prev,
                                                board: { ...prev.board, grid: newGrid },
                                                animation: newAnimation
                                            };
                                        });

                                        const currentStep = animationPath[stepIndex];
                                        const pauseTime = currentStep.pause;

                                        setTimeout(() => {
                                            animateStep(stepIndex + 1);
                                        }, pauseTime);

                                    }, stepIndex === 0 ? 0 : animationPath[stepIndex - 1].duration);
                                };

                                // Start capture
                                animateStep(0);

                                return updatedGame;
                            }

                            case "PROMOTED_PIECE": {
                                const { row, col, pieceColor } = response;
                                console.log(`PROMOTED_PIECE: (${row}, ${col}) for ${pieceColor}`);

                                setGame(prevGame => {
                                    if (!prevGame) return prevGame;

                                    if (
                                        prevGame.animation &&
                                        prevGame.animation.type === "CAPTURE" &&
                                        prevGame.animation.finalPosition.row === row &&
                                        prevGame.animation.finalPosition.col === col
                                    ) {
                                        // Store promotion intent on animation state instead of using a racing setTimeout.
                                        // The animation completion block will apply it atomically in the same setGame call.
                                        return {
                                            ...prevGame,
                                            animation: {
                                                ...prevGame.animation,
                                                pendingPromotion: { row, col, pieceColor }
                                            }
                                        };
                                    } else {
                                        // No capture animation running, apply promotion immediately
                                        const newGrid = prevGame.board.grid.map(r => [...r]);
                                        const piece = newGrid[row][col];
                                        if (piece && piece.color.toLowerCase() === pieceColor.toLowerCase()) {
                                            newGrid[row][col] = { ...piece, king: true };
                                        }
                                        return { ...prevGame, board: { ...prevGame.board, grid: newGrid } };
                                    }
                                });

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
                                const { winnerName, winnerColor, gameResult } = response;
                                console.log(`Game over! Winner: ${winnerName} (${winnerColor})`);

                                updatedGame.currentTurn = null;
                                updatedGame.allowedMoves = [];
                                updatedGame.winner = { name: winnerName, color: winnerColor, result: gameResult };

                                break;
                            }

                            case "GAME_DRAW": {
                                const { drawReason } = response;
                                console.log(`Game draw! Draw reason: ${drawReason}`);

                                updatedGame.currentTurn = null;
                                updatedGame.allowedMoves = [];
                                updatedGame.winner = null;
                                updatedGame.drawReason = drawReason;
                                updatedGame.draw = true;

                                break;
                            }

                            case "GAME_FORFEIT": {
                                const { winnerName, winnerColor, gameResult } = response;
                                console.log(`Game forfeited! Winner: ${winnerName}, Result: ${gameResult}`);

                                updatedGame.currentTurn = null;
                                updatedGame.allowedMoves = [];
                                updatedGame.winner = { name: winnerName, color: winnerColor, result: gameResult };

                                break;
                            }

                            case "INVALID_MOVE": {
                                console.warn("Invalid move detected");
                                setSelectedCell(null);

                                break;
                            }

                            case "REMOVED_PIECE": {
                                const { pieceColor } = response;

                                console.log('Removed piece: ', pieceColor)

                                if (pieceColor === "RED") {
                                    setRemovedRedPieces(prev => prev + 1);
                                } else if (pieceColor === "WHITE") {
                                    setRemovedWhitePieces(prev => prev + 1);
                                } else {
                                    console.warn("Unknown pieceColor in REMOVED_PIECE:", pieceColor);
                                }

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

    const handleCellClick = useCallback(
        (row, col) => {
            if (!game) return;

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
                    move =>
                        move.fromRow === selectedCell.row &&
                        move.fromCol === selectedCell.col &&
                        move.toRow === row &&
                        move.toCol === col
                );

                if (!isMoveAllowed) {
                    setSelectedCell(null);
                    return;
                }

                const moveDto = {
                    fromRow: selectedCell.row,
                    fromCol: selectedCell.col,
                    toRow: row,
                    toCol: col,
                };

                sendMessage(`/app/games/${gameId}/move`, JSON.stringify(moveDto));
                setSelectedCell(null);
            }
        },
        [game, selectedCell, sendMessage, gameId]
    );


    const handleForfeit = useCallback(async () => {
        if (!game) return;

        try {
            await ApiService.post(`/games/${gameId}/forfeit`, {
                pieceColor: game.playerColor,
            });
        } catch {
            toast.error("Failed to forfeit the game. Please try again.");
        }
    }, [game, gameId]);

    if (loading) return <Loader />;
    if (!game) return null;

    console.log('Game: ', game);

    const playerColor = game.playerColor?.toUpperCase();

    const enemyColor =
        playerColor === "WHITE" ? "RED" : "WHITE";

    if (showIntro) {
        return (
            <GameIntro
                playerOne={user.displayName || playerColor}
                playerOneColor={playerColor}
                playerTwo={game.enemyDisplayName || enemyColor}
                playerTwoColor={enemyColor}
                onFinish={() => setShowIntro(false)}
            />
        );
    }

    return (
        <div className={styles.page}>
            <div className={styles.mainContainer}>
                <div className={styles.sidePanel}>
                    <div className={styles.header}>
                        <h2>
                            You are
                            <span
                                className={`${styles.playerColor} ${styles[game.playerColor.toLowerCase()]}`}
                            >
                            {game.playerColor.toLowerCase()}
                        </span>
                        </h2>

                        <p className={styles.turnInfo}>
                            Current turn:
                            <span
                                className={`${styles.turnColor} ${
                                    game.currentTurn ? styles[game.currentTurn.toLowerCase()] : ""
                                }`}
                            >
                            {game.currentTurn ? game.currentTurn.toLowerCase() : "-"}
                        </span>
                        </p>

                        <Button
                            onClick={handleForfeit}
                            disabled={!game.currentTurn || game.winner}
                            children="Forfeit"
                        />
                    </div>

                    <div className={styles.captured}>
                        <h4>Captured pieces</h4>
                        <div className={styles.capturedRow}>
                        <span className={`${styles.badge} ${styles.red}`}>
                            Red: {removedRedPieces}
                        </span>
                            <span className={`${styles.badge} ${styles.white}`}>
                            White: {removedWhitePieces}
                        </span>
                        </div>
                    </div>
                </div>

                <GameBoard
                    board={game.board}
                    allowedMoves={game.allowedMoves}
                    selectedCell={selectedCell}
                    onCellClick={handleCellClick}
                    playerColor={game.playerColor}
                />
            </div>
        </div>
    );
};

export default Game;