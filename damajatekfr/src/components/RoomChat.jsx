import { useEffect, useRef, useState } from "react";
import styles from "./RoomChat.module.css";
import Button from "../components/Button.jsx";
import { useSharedWebSocket } from "../contexts/WebSocketContext.jsx";

const RoomChat = ({ currentUser, roomId }) => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const bottomRef = useRef(null);

    const {
        isConnected,
        subscribe,
        sendMessage: sendWsMessage
    } = useSharedWebSocket();

    useEffect(() => {
        if (!isConnected || !roomId) return;

        const destination = `/topic/room/${roomId}/chat`;

        const unsubscribe = subscribe(
            destination,
            (message) => {
                try {
                    const response = JSON.parse(message.body);
                    setMessages(prev => [...prev, response]);
                } catch (e) {
                    console.error("Failed to parse chat message:", e);
                }
            },
            [roomId]
        );

        return () => unsubscribe();
    }, [isConnected, roomId, subscribe]);

    const sendMessage = () => {
        if (!input.trim() || !isConnected) return;

        const dto = {
            content: input.trim()
        };

        sendWsMessage(`/app/room/${roomId}/chat/send`, JSON.stringify(dto));
        setInput("");
    };

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    return (
        <div className={styles.chatSection}>
            <h3 className={styles.chatTitle}>Room Chat</h3>

            <div className={styles.chatMessages}>
                {messages.map(msg => (
                    <div
                        key={msg.id}
                        className={`${styles.chatMessage} ${
                            msg.sender === currentUser
                                ? styles.chatMessageSelf
                                : styles.chatMessageOther
                        }`}
                    >
                        <span className={styles.chatSender}>{msg.sender}</span>
                        <span className={styles.chatContent}>{msg.content}</span>
                    </div>
                ))}
                <div ref={bottomRef} />
            </div>

            <div className={styles.chatInputRow}>
                <input
                    type="text"
                    placeholder="Type a message..."
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && sendMessage()}
                    className={styles.chatInput}
                />
                <Button onClick={sendMessage}>Send</Button>
            </div>
        </div>
    );
};

export default RoomChat;
