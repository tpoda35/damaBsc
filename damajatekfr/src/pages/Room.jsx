import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ApiService from "../services/ApiService";
import {useSharedWebSocket} from "../contexts/WebSocketContext.jsx";

const Room = () => {
    const { roomId } = useParams();
    const [room, setRoom] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const ws = useSharedWebSocket();

    useEffect(() => {
        const fetchRoom = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await ApiService.get(`/rooms/${roomId}`);
                console.log("roomData: ", data);
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
                const client = await ws.connect();

                unsubscribeFn = ws.subscribe(
                    `/topic/rooms/${roomId}`,
                    (message) => {
                        try {
                            const body = JSON.parse(message.body);
                            console.log("[WS] Room update:", body);
                            setRoom((prev) => ({ ...prev, ...body }));
                        } catch (e) {
                            console.error("Invalid WS message:", e);
                        }
                    }
                );

                console.log("[WS] Subscribed to /topic/rooms/" + roomId);
            } catch (e) {
                console.error("[WS] Failed to connect/subscribe:", e);
            }
        };

        connectAndSubscribe();

        return () => {
            if (unsubscribeFn) unsubscribeFn();
        };
    }, [roomId]);

    if (loading) return <div>Loading room...</div>;
    if (error) return <div>Error: {error}</div>;
    if (!room) return <div>Room not found</div>;

    const { id, name, host, opponent } = room;

    return (
        <div>
            <h2>Room: {name || `Room ${id}`}</h2>

            <div>
                <h3>Players</h3>

                <div>
                    <div>
                        <strong>Host:</strong> {host?.displayName || "Unknown"}
                        <div>Status: {host?.readyStatus || "WAITING"}</div>
                    </div>

                    <div>
                        <strong>Opponent:</strong>{" "}
                        {opponent ? opponent.displayName : "Waiting for opponent..."}
                        <div>Status: {opponent?.readyStatus || "N/A"}</div>
                    </div>
                </div>
            </div>

            <div>
                Waiting for both players to be ready...
            </div>
        </div>
    );
};

export default Room;
