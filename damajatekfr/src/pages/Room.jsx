import { useEffect, useState, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ApiService from "../services/ApiService";
import { useSharedWebSocket } from "../contexts/WebSocketContext.jsx";

const Room = () => {
    const { roomId } = useParams();
    const [room, setRoom] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const hasLeftRoom = useRef(false);

    const ws = useSharedWebSocket();
    const navigate = useNavigate();

    const handleLeaveRoom = async () => {
        if (hasLeftRoom.current) return;
        hasLeftRoom.current = true;

        try {
            await ApiService.post(`/rooms/${roomId}/leave`);
            navigate("/rooms");
        } catch (err) {
            setError(err.message || "Failed to leave room");
            hasLeftRoom.current = false;
        }
    };

    const handleKickOpponent = async () => {
        try {
            await ApiService.post(`/rooms/${roomId}/kick`);
        } catch (err) {
            setError(err.message || "Failed to kick opponent");
        }
    };

    const handleToggleReady = async () => {
        try {
            await ApiService.post(`/rooms/${roomId}/ready`);
        } catch (err) {
            setError(err.message || "Failed to toggle ready status");
        }
    };

    const handleStartGame = async () => {
        try {
            await ApiService.post(`/rooms/${roomId}/start`);
        } catch (err) {
            setError(err.message || "Failed to start game");
        }
    };

    useEffect(() => {
        const fetchRoom = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await ApiService.get(`/rooms/${roomId}`);
                setRoom(data);
            } catch (err) {
                setError(err.message || "Failed to fetch room info");
            } finally {
                setLoading(false);
            }
        };

        if (roomId) fetchRoom();
    }, [roomId]);

    useEffect(() => {
        if (!ws || !roomId) return;

        let unsubscribeFn = null;

        const connectAndSubscribe = async () => {
            try {
                unsubscribeFn = ws.subscribe(`/topic/rooms/${roomId}`, (message) => {
                    try {
                        const body = JSON.parse(message.body);
                        const { action, player } = body;

                        switch (action) {
                            case "OPPONENT_JOIN":
                                setRoom((prev) => {
                                    if (prev?.isHost) {
                                        console.log(`[WS] Opponent joined:`, player);
                                        return {
                                            ...prev,
                                            opponent: player
                                        };
                                    }
                                    return prev;
                                });
                                break;

                            case "OPPONENT_LEAVE":
                                setRoom((prev) => {
                                    if (prev?.isHost) {
                                        console.log(`[WS] Opponent left.`);
                                        return {
                                            ...prev,
                                            opponent: null
                                        };
                                    }
                                    return prev;
                                });
                                break;

                            case "HOST_LEAVE":
                                console.warn(`[WS] Host left, room ${roomId} is closing`);
                                navigate("/rooms");
                                break;

                            case "KICK":
                                setRoom((prev) => {
                                    if (!prev?.isHost) {
                                        console.warn(`[WS] You were kicked from room ${roomId}`);
                                        navigate("/rooms");
                                        return prev;
                                    } else {
                                        console.log(`[WS] Opponent was kicked.`);
                                        return {
                                            ...prev,
                                            opponent: null
                                        };
                                    }
                                });
                                break;

                            case "HOST_READY":
                                console.log(`[WS] Host ready status:`, player?.readyStatus);
                                setRoom((prev) => ({
                                    ...prev,
                                    host: {
                                        ...prev.host,
                                        readyStatus: player?.readyStatus
                                    }
                                }));
                                break;

                            case "OPPONENT_READY":
                                console.log(`[WS] Opponent ready status:`, player?.readyStatus);
                                setRoom((prev) => ({
                                    ...prev,
                                    opponent: {
                                        ...prev.opponent,
                                        readyStatus: player?.readyStatus
                                    }
                                }));
                                break;

                            case "START":
                                console.log(`[WS] Game started in room ${roomId}`);
                                navigate(`/rooms/${roomId}/game`);
                                break;

                            default:
                                console.warn("[WS] Unknown action:", action);
                        }
                    } catch (e) {
                        console.error("Invalid WS message:", e);
                    }
                });

                console.log("[WS] Subscribed to /topic/rooms/" + roomId);
            } catch (e) {
                console.error("[WS] Failed to connect/subscribe:", e);
            }
        };

        connectAndSubscribe();

        return () => {
            if (unsubscribeFn) {
                unsubscribeFn();
                console.log("[WS] Unsubscribed from /topic/rooms/" + roomId);
            }
        };
    }, [ws, roomId, navigate]);

    // React unmount (navigation within app)
    useEffect(() => {
        if (!roomId) return;

        return () => {
            if (!hasLeftRoom.current) {
                hasLeftRoom.current = true;
                ApiService.post(`/rooms/${roomId}/leave`).catch(() => {});
            }
        };
    }, [roomId]);

    // Browser close/reload - use sendBeacon
    useEffect(() => {
        if (!roomId) return;

        const handleBeforeUnload = () => {
            if (!hasLeftRoom.current) {
                hasLeftRoom.current = true;

                try {
                    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL || "";
                    const url = `${backendBaseUrl}/rooms/${roomId}/leave`;
                    const blob = new Blob([JSON.stringify({})], { type: "application/json" });

                    const ok = navigator.sendBeacon(url, blob);
                    console.log(`[Beacon] Leave room ${roomId} sent:`, ok);
                } catch (err) {
                    console.warn(`[Beacon] Failed to send leave for room ${roomId}:`, err);
                }
            }
        };

        window.addEventListener("beforeunload", handleBeforeUnload);

        return () => {
            window.removeEventListener("beforeunload", handleBeforeUnload);
        };
    }, [roomId]);

    if (loading) return <div>Loading room...</div>;
    if (error) return <div>Error: {error}</div>;
    if (!room) return <div>Room not found</div>;

    const { id, name, host, opponent, isHost } = room;

    return (
        <div>
            <h2>Room: {name || `Room ${id}`}</h2>

            <div>
                <h3>Players</h3>

                <div>
                    <div>
                        <strong>Host:</strong> {host?.displayName || "Unknown"}
                        <div>Status: {host?.readyStatus || "NOT_READY"}</div>
                    </div>

                    <div>
                        <strong>Opponent:</strong>{" "}
                        {opponent ? opponent.displayName : "Waiting for opponent..."}
                        <div>Status: {opponent?.readyStatus || "N/A"}</div>
                    </div>
                </div>
            </div>

            <div>
                <button onClick={handleToggleReady}>
                    {isHost
                        ? (host?.readyStatus === "READY" ? "Unready" : "Ready")
                        : (opponent?.readyStatus === "READY" ? "Unready" : "Ready")
                    }
                </button>

                {isHost && opponent && (
                    <button onClick={handleKickOpponent}>Kick Opponent</button>
                )}

                <button onClick={handleLeaveRoom}>Leave Room</button>

                {isHost && (
                    <button onClick={handleStartGame}>Start Game</button>
                )}
            </div>

            <div>
                Waiting for both players to be ready...
            </div>
        </div>
    );
};

export default Room;