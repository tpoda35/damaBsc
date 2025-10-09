import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import ApiService from "../services/ApiService";
import Modal from "../components/Modal";
import Form from "../components/Form.jsx";

const Rooms = () => {
    const [rooms, setRooms] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        const fetchRooms = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await ApiService.get("/rooms");
                setRooms(data);
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

    const handleCreateRoom = async (formData) => {
        try {
            const roomId = await ApiService.post("/rooms", formData);
            console.log(roomId);
            setIsModalOpen(false);
            navigate(`/rooms/${roomId}`);
        } catch (err) {
            setError(err.message || "Failed to create room");
        }
    };

    const fields = [
        { label: "Room name", name: "name", type: "text", placeholder: "MyRoom123", required: true },
        { label: "Locked", name: "locked", type: "checkbox", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
    ];

    if (loading) return <div>Loading rooms...</div>;
    if (error) return <div>Error: {error}</div>;

    const roomList = rooms?.content || [];

    return (
        <div>
            <button onClick={handleHostRoom}>Host room</button>

            {roomList.length === 0 ? (
                <div>No rooms available</div>
            ) : (
                <ul>
                    {roomList.map((room) => (
                        <li key={room.id}>
                            {room.name || `Room ${room.id}`} -{" "}
                            {room.content || "No content"}
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
                        // Only show password field if locked is true
                        if (field.name === "password") return formData.locked;
                        return true;
                    }}
                />
            </Modal>
        </div>
    );
};

export default Rooms;
