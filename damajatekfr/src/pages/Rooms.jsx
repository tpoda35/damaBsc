import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../services/ApiService";
import Modal from "../components/Modal";
import Form from "../components/Form.jsx";
import styles from './Rooms.module.css';
import Button from "../components/Button.jsx";
import { toast } from "react-toastify";
import { getErrorMessage } from "../utils/getErrorMessage.js";
import Loader from "../components/Loader.jsx";
import { withToastError } from "../utils/withToastError.js";
import AnimatedPage from "../components/AnimatedPage.jsx";

const Rooms = () => {
    const [roomsPage, setRoomsPage] = useState(null);
    const [loading, setLoading] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [pageNum, setPageNum] = useState(0);
    const [pageSize] = useState(6);

    const navigate = useNavigate();

    const fetchRooms = async (page = 0, size = 10) => {
        setLoading(true);
        setError(null);
        try {
            const data = await withToastError(
                () => ApiService.get(`/rooms?pageNum=${page}&pageSize=${size}`),
                "Failed to fetch rooms"
            );
            setRoomsPage(data);
        } catch (err) {
            setError(getErrorMessage(err, "Failed to fetch rooms"));
        } finally {
            setLoading(false);
        }
    };

    const refreshRooms = async (page = 0, size = 10) => {
        setRefreshing(true);
        setError(null);

        try {
            const data = await withToastError(
                () => ApiService.get(`/rooms?pageNum=${page}&pageSize=${size}`),
                "Failed to fetch rooms"
            );
            setRoomsPage(data);
        } catch (err) {
            setError(getErrorMessage(err, "Failed to fetch rooms"));
        } finally {
            setRefreshing(false);
        }
    };


    useEffect(() => {
        fetchRooms(pageNum, pageSize);
    }, [pageNum, pageSize]);

    const handleHostRoom = () => {
        setIsModalOpen(true);
    };

    const handleRefresh = () => {
        refreshRooms(pageNum, pageSize);
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
            toast.error(getErrorMessage(err, "Failed to join room"));
        }
    };

    const handleCreateRoom = async (formData) => {
        try {
            const roomId = await ApiService.post("/rooms", formData);
            setIsModalOpen(false);
            navigate(`/rooms/${roomId}`);
        } catch (err) {
            toast.error(getErrorMessage(err, "Failed to create room"));
        }
    };

    const fields = [
        { label: "Room name", name: "name", type: "text", placeholder: "MyRoom123", required: true },
        { label: "Room description", name: "description", type: "text", placeholder: "MyRoomDescription123", required: false },
        { label: "Locked", name: "locked", type: "checkbox", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
    ];

    if (loading) return <Loader />;

    const roomList = roomsPage?.content || [];
    const totalPages = roomsPage?.totalPages || 1;

    console.log('Refreshing: ', refreshing);

    return (
        <>
            <AnimatedPage className={styles.rooms}>
                <div className={styles.header}>
                    <h2 className={styles.title}>Room list</h2>
                    <div className={styles.headerButtons}>
                        <Button onClick={handleHostRoom} children="Host room" />
                        <Button onClick={handleRefresh} disabled={refreshing}>
                            <i
                                className={`fa ${refreshing ? "fa-spinner fa-spin" : "fa-refresh"}`}
                                aria-hidden="true"
                            />
                        </Button>

                    </div>
                </div>

                {roomList.length === 0 ? (
                    <div className={styles.empty}>No rooms available</div>
                ) : (
                    <>
                        <ul className={styles.roomList}>
                            {roomList.map((room) => (
                                <li className={styles.roomCard} key={room.id}>
                                    <div>
                                        <strong>{room.name || `Room ${room.id}`}</strong>
                                        <div className={styles.roomDesc}>
                                            {room.description || "No description"}
                                        </div>
                                    </div>

                                    <div className={styles.roomMeta}>
                                        {room.locked && (
                                            <span className={styles.infoBox}>
                                            <i className="fa fa-lock" aria-hidden="true"></i>
                                        </span>
                                        )}

                                        <span className={styles.infoBox}>
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

                        <div className={styles.pagination}>
                            <Button
                                onClick={() => setPageNum((prev) => Math.max(prev - 1, 0))}
                                disabled={pageNum === 0}
                                children="Prev"
                            />
                            <span className={styles.pageInfo}>
                            Page {pageNum + 1} of {totalPages}
                        </span>
                            <Button
                                onClick={() => setPageNum((prev) => Math.min(prev + 1, totalPages - 1))}
                                disabled={pageNum + 1 >= totalPages}
                                children="Next"
                            />
                        </div>
                    </>
                )}


            </AnimatedPage>

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
        </>
    );
};

export default Rooms;
