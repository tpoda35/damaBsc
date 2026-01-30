import {useEffect, useRef, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import ApiService from "../services/ApiService";
import {useSharedWebSocket} from "../contexts/WebSocketContext.jsx";
import styles from './Room.module.css';
import Button from "../components/Button.jsx";
import Loader from "../components/Loader.jsx";
import {withToastError} from "../utils/withToastError.js";
import RoomChat from "../components/RoomChat.jsx";
import {toast} from "react-toastify";

const Room = () => {
    const { roomId } = useParams();
    const [room, setRoom] = useState(null);
    const [loading, setLoading] = useState(true);
    const hasLeftRoom = useRef(false);

    const ws = useSharedWebSocket();
    const navigate = useNavigate();

    const handleLeaveRoom = async () => {
        if (hasLeftRoom.current) return;
        hasLeftRoom.current = true;

        try {
            await withToastError(
                () => ApiService.post(`/rooms/${roomId}/leave`),
                "Failed to leave room"
            );
            navigate("/rooms");
        } catch {
            hasLeftRoom.current = false;
        }
    };

    const handleKickOpponent = async () => {
        await withToastError(
            () => ApiService.post(`/rooms/${roomId}/kick`),
            "Failed to kick opponent"
        );
    };

    const handleToggleReady = async () => {
        await withToastError(
            () => ApiService.post(`/rooms/${roomId}/ready`),
            "Failed to toggle ready status"
        );
    };

    const handleStartGame = async () => {
        await withToastError(
            () => ApiService.post(`/rooms/${roomId}/start`),
            "Failed to start game"
        );
    };

    useEffect(() => {
        const fetchRoom = async () => {
            setLoading(true);
            try {
                const fetchedRoom = await withToastError(
                    () => ApiService.get(`/rooms/${roomId}`),
                    "Failed to fetch room info"
                );
                setRoom(fetchedRoom);
            } catch {
                setRoom(null);
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
                        const { action, player, gameId } = body;

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
                                        toast.warn("Opponent left");
                                        return {
                                            ...prev,
                                            opponent: null
                                        };
                                    }
                                    return prev;
                                });
                                break;

                            case "HOST_LEAVE":
                                if (!room?.isHost) {
                                    toast.warn("Host left, room closed");
                                }
                                console.warn(`[WS] Host left, room ${roomId} is closing`);
                                hasLeftRoom.current = true;
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
                                console.log(`[WS] Game started in room ${roomId}, with gameId ${gameId}`);
                                navigate(`/games/${gameId}`);
                                break;

                            default:
                                console.warn("[WS] Unknown action:", action);
                        }
                    } catch (e) {
                        console.error("Invalid WS message:", e);
                    }
                });
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
                ApiService.post(`/rooms/${roomId}/leave`).catch(err =>
                    console.warn("Failed to auto-leave room:", err)
                );
            }
        };
    }, [roomId]);

    // Browser close/reload
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

    if (loading) return <Loader />;

    if (!room) {
        return (
            <div className={styles.container}>
                <h2>Room not found</h2>
                <Button onClick={() => navigate("/rooms")}>
                    Back to Rooms
                </Button>
            </div>
        );
    }

    const { id, name, host, opponent, isHost } = room;

    return (
        <div className={styles.container}>
            <h2 className={styles.title}>
                Room: {name || `Room ${id}`}
            </h2>

            <div className={styles.playersSection}>
                <h3 className={styles.playersTitle}>Players</h3>

                <div className={styles.playersGrid}>
                    <div className={styles.playerCard}>
                        <div className={styles.playerName}>
                            Host: {host?.displayName || "Unknown"}
                        </div>
                        <div
                            className={`${styles.status} ${
                                host?.readyStatus === "READY"
                                    ? styles.statusReady
                                    : styles.statusNotReady
                            }`}
                        >
                            Status: {host?.readyStatus || "NOT_READY"}
                        </div>
                    </div>

                    <div className={styles.playerCard}>
                        <div className={styles.playerName}>
                            Opponent:{" "}
                            {opponent ? opponent.displayName : "Waiting for opponent..."}
                        </div>
                        <div
                            className={`${styles.status} ${
                                opponent?.readyStatus === "READY"
                                    ? styles.statusReady
                                    : opponent
                                        ? styles.statusNotReady
                                        : styles.statusWaiting
                            }`}
                        >
                            Status: {opponent?.readyStatus || "N/A"}
                        </div>
                    </div>
                </div>
            </div>

            <RoomChat
                roomId={roomId}
                chatMessages={room.messages}
            />

            <div className={styles.actions}>
                <Button
                    onClick={handleToggleReady}
                    children={
                    isHost
                        ? host?.readyStatus === "READY"
                            ? "Unready"
                            : "Ready"
                        : opponent?.readyStatus === "READY"
                            ? "Unready"
                            : "Ready"
                }
                />

                {isHost && opponent && (
                    <Button
                        onClick={handleKickOpponent}
                        children="Kick Opponent"
                    />
                )}

                <Button
                    onClick={handleLeaveRoom}
                    children="Leave Room"
                />

                {isHost && (
                    <Button
                        onClick={handleStartGame}
                        children="Start Game"
                    />
                )}
            </div>

            <div className={styles.info}>
                Waiting for both players to be ready...
            </div>
        </div>
    );
};

export default Room;