import {NavLink, useNavigate} from "react-router-dom";
import {useSharedAuth} from "../contexts/AuthContext";
import React, {useEffect, useRef, useState} from "react";
import styles from "./Navbar.module.css";
import Modal from "./Modal.jsx";
import Form from "./Form.jsx";
import ApiService from "../services/ApiService.js";

const Navbar = () => {
    const { user, logout } = useSharedAuth();
    const navigate = useNavigate();

    const [playOpen, setPlayOpen] = useState(false);
    const playRef = useRef(null);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalError, setModalError] = useState(null);

    const handleCreateAIGame = async (formData) => {
        try {
            const gameId = await ApiService.post("/games/ai/start", formData);
            setIsModalOpen(false);
            navigate(`/games/${gameId}`);
        } catch (err) {
            setModalError(err.message || "Failed to create room");
        }
    };

    // Dropdown outside click
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (playRef.current && !playRef.current.contains(event.target)) {
                setPlayOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    const getNavLinkClass = ({ isActive }) =>
        isActive
            ? `${styles.navbarButton} ${styles.navbarButtonActive}`
            : styles.navbarButton;

    const fields = [
        {
            label: "Difficulty",
            name: "botDifficulty",
            type: "select",
            required: true,
            options: [
                { label: "Easy", value: "EASY" },
                { label: "Medium", value: "MEDIUM" },
                { label: "Hard", value: "HARD" },
            ],
        }
    ];

    return (
        <nav className={styles.navbar}>
            <NavLink to="/" className={styles.logoBtn}>
                Checkers
            </NavLink>

            {!user ? (
                <div className={styles.buttonContainer}>
                    <NavLink to="/login" className={getNavLinkClass}>
                        Login
                    </NavLink>
                    <NavLink to="/register" className={getNavLinkClass}>
                        Register
                    </NavLink>
                </div>
            ) : (
                <div className={styles.buttonContainer}>

                    <div className={styles.dropdown} ref={playRef}>
                        <button
                            className={styles.navbarButton}
                            onClick={() => setPlayOpen((prev) => !prev)}
                        >
                            Play ▾
                        </button>

                        {playOpen && (
                            <div className={styles.dropdownMenu}>
                                <button
                                    className={styles.dropdownItem}
                                    onClick={() => {
                                        setPlayOpen(false);
                                        navigate("/rooms");
                                    }}
                                >
                                    Play Online
                                </button>

                                <button
                                    className={styles.dropdownItem}
                                    onClick={() => {
                                        setPlayOpen(false);
                                        setIsModalOpen(true);
                                    }}
                                >
                                    Play vs AI
                                </button>
                            </div>
                        )}
                    </div>

                    <NavLink to="/profile" className={getNavLinkClass}>
                        Profile
                    </NavLink>
                    <button onClick={logout} className={styles.navbarButton}>
                        Logout
                    </button>
                </div>
            )}

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title="Set up AI game"
            >
                <Form
                    fields={fields}
                    buttonText="Play"
                    onSubmit={handleCreateAIGame}
                    error={modalError}
                />
            </Modal>
        </nav>
    );
};

export default Navbar;
