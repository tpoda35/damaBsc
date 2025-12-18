import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import ApiService from "../services/ApiService";
import Modal from "../components/Modal";
import Form from "../components/Form.jsx";
import styles from './Rooms.module.css';
import Button from "../components/Button.jsx";

const Rooms = () => {
    const [rooms, setRooms] = useState(null);
    console.log('Rooms: ', rooms);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        const fetchRooms = async () => {
            setLoading(true);
            setError(null);
            try {
                const rooms = await ApiService.get("/rooms");
                setRooms(rooms);
            } catch (err) {
                setError(err.message || "Failed to fetch rooms");
            } finally {
                setLoading(false);
            }
        };

        fetchRooms();
    }, []);

    const handleHostRoom = () => {
        setIsModalOpen(true);
    };

    const handleJoinRoom = async (room) => {
        try {
            let joinedRoomId;

            if (room.locked) {
                const password = prompt(`Enter password for room "${room.name}"`);
                if (!password || password.trim() === "") return;

                joinedRoomId = await ApiService.post(`/rooms/${room.id}/join`, { password });
            } else {
                joinedRoomId = await ApiService.post(`/rooms/${room.id}/join`, null);
            }

            navigate(`/rooms/${joinedRoomId}`);
        } catch (err) {
            setError(err.message || "Failed to join room");
        }
    };

    const handleCreateRoom = async (formData) => {
        try {
            console.log("formData: ", formData);
            const roomId = await ApiService.post("/rooms", formData);
            setIsModalOpen(false);
            navigate(`/rooms/${roomId}`);
        } catch (err) {
            setError(err.message || "Failed to create room");
        }
    };

    const fields = [
        { label: "Room name", name: "name", type: "text", placeholder: "MyRoom123", required: true },
        { label: "Room description", name: "description", type: "text", placeholder: "MyRoomDescription123", required: false },
        { label: "Locked", name: "locked", type: "checkbox", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
    ];

    if (loading) return <div>Loading rooms...</div>;
    if (error) return <div>Error: {error}</div>;

    const roomList = rooms?.content || [];

    return (
        <div className={styles.rooms}>
            {/* Header */}
            <div className={styles.header}>
                <h2 className={styles.title}>Room list</h2>

                <Button
                    onClick={handleHostRoom}
                    children="Host room"
                />
            </div>

            {roomList.length === 0 ? (
                <div className={styles.empty}>No rooms available</div>
            ) : (
                <ul className={styles.roomList}>
                    {roomList.map((room) => (
                        <li className={styles.roomCard} key={room.id}>
                            <div>
                                <strong className={styles.roomTitle}>
                                    {room.name || `Room ${room.id}`}
                                    {room.locked && <i className="fa fa-lock" aria-hidden="true"></i>}
                                </strong>
                                <div className={styles.roomDesc}>
                                    {room.description || "No description"}
                                </div>
                            </div>

                            <div className={styles.roomMeta}>
                                <span className={styles.players}>
                                    {room.playerCount}/2
                                </span>

                                    <Button
                                        onClick={() => handleJoinRoom(room)}
                                        children="Join room"
                                    />
                                </div>
                        </li>
                    ))}
                </ul>
            )}

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title="Host a New Room"
            >
                <Form
                    fields={fields}
                    onSubmit={handleCreateRoom}
                    buttonText="Host"
                    error={error}
                    conditionalRender={(formData, field) => {
                        if (field.name === "password") return formData.locked;
                        return true;
                    }}
                />
            </Modal>
        </div>
    );
};

export default Rooms;
