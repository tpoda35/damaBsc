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
import Pagination from "../components/Pagination.jsx";
import {useMinimumLoading} from "../utils/useMinimumLoading.js";

const Rooms = () => {
    const [roomsPage, setRoomsPage] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const showLoader = useMinimumLoading(loading, 500);
    const [refreshing, setRefreshing] = useState(false);

    const [isCreateRoomModalOpen, setIsCreateRoomModalOpen] = useState(false);

    const [pageNum, setPageNum] = useState(0);
    const [pageSize] = useState(6);

    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
    const [selectedRoom, setSelectedRoom] = useState(null);

    const navigate = useNavigate();

    const fetchRooms = async (page = pageNum, size = pageSize) => {
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
        setIsCreateRoomModalOpen(true);
    };

    const handleRefreshRooms = () => {
        refreshRooms(pageNum, pageSize);
    };

    const handleJoinRoom = async (room) => {
        try {
            if (room.locked) {
                setSelectedRoom(room);
                setIsPasswordModalOpen(true);
                return;
            }

            const joinedRoomId = await ApiService.post(`/rooms/${room.id}/join`, null);
            navigate(`/rooms/${joinedRoomId}`);
        } catch (err) {
            toast.error(getErrorMessage(err, "Failed to join room"));
        }
    };

    const handlePasswordSubmit = async (formData) => {
        try {
            setError(null);

            const joinedRoomId = await ApiService.post(
                `/rooms/${selectedRoom.id}/join`,
                { password: formData.password }
            );

            setIsPasswordModalOpen(false);
            setSelectedRoom(null);
            navigate(`/rooms/${joinedRoomId}`);
        } catch (err) {
            setError(getErrorMessage(err, "Failed to join room"));
        }
    };

    const handleCreateRoom = async (formData) => {
        try {
            const roomId = await ApiService.post("/rooms", formData);
            setIsCreateRoomModalOpen(false);
            navigate(`/rooms/${roomId}`);
        } catch (err) {
            toast.error(getErrorMessage(err, "Failed to create room"));
        }
    };

    const createRoomFields = [
        { label: "Room name", name: "name", type: "text", placeholder: "MyRoom123", required: true },
        { label: "Room description", name: "description", type: "text", placeholder: "MyRoomDescription123", required: false },
        { label: "Locked", name: "locked", type: "checkbox", required: true },
        { label: "Password", name: "password", type: "password", placeholder: "********", required: true },
    ];

    const passwordFields=[
            { label: "Password", name: "password", type: "password", placeholder: "Enter room password", required: true },
    ];

    const roomList = roomsPage?.content || [];
    const totalPages = roomsPage?.totalPages || 1;

    return (
        <>
            {showLoader ? (
                <Loader />
            ) : (
                <AnimatedPage className={styles.rooms}>
                    <div className={styles.header}>
                        <h2 className={styles.title}>Room list</h2>
                        <div className={styles.headerButtons}>
                            <Button onClick={handleHostRoom} children="Host room" />
                            <Button onClick={handleRefreshRooms} disabled={refreshing}>
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

                            <Pagination
                                pageNum={pageNum}
                                totalPages={totalPages}
                                onPageChange={setPageNum}
                            />
                        </>
                    )}
                </AnimatedPage>
            )}


            <Modal
                isOpen={isCreateRoomModalOpen}
                onClose={() => setIsCreateRoomModalOpen(false)}
                title="Host a New Room"
            >
                <Form
                    fields={createRoomFields}
                    onSubmit={handleCreateRoom}
                    buttonText="Host"
                    error={error}
                    conditionalRender={(formData, field) => {
                        if (field.name === "password") return formData.locked;
                        return true;
                    }}
                />
            </Modal>

            <Modal
                isOpen={isPasswordModalOpen}
                onClose={() => {
                    setIsPasswordModalOpen(false);
                    setSelectedRoom(null);
                    setError(null);
                }}
                title={`Enter password for "${selectedRoom?.name}"`}
            >
                <Form
                    fields={passwordFields}
                    onSubmit={handlePasswordSubmit}
                    buttonText="Join Room"
                    error={error}
                />
            </Modal>
        </>
    );
};

export default Rooms;
