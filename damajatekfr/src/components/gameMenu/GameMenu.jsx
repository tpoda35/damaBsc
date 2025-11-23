import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import Button from "../Button.jsx";
import Modal from "../Modal.jsx";
import Form from "../Form.jsx";
import ApiService from "../../services/ApiService.js";

const GameMenu = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [error, setError] = useState(null);

    const navigate = useNavigate();

    const handleRoomsRedirect = () => {
        navigate("/rooms");
    };

    const handleCreateAIGame = async (formData) => {
        try {
            const gameId = await ApiService.post("/games/ai/start", formData);
            setIsModalOpen(false);
            navigate(`/games/${gameId}`);
        } catch (err) {
            setError(err.message || "Failed to create room");
        }
    };

    const fields = [
        { label: "Difficulty", name: "botDifficulty", type: "select", required: true, options: [
                { label: "Easy", value: "EASY" },
                { label: "Medium", value: "MEDIUM" },
                { label: "Hard", value: "HARD" },
            ],
        }
    ];

    return (
        <div>
            <Button
                onClick={() => setIsModalOpen(true)}
            >
                Play vs AI
            </Button>

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title="Set up AI game"
            >
                <Form
                    fields={fields}
                    buttonText="Play"
                    onSubmit={handleCreateAIGame}
                    error={error}
                />
            </Modal>

            <Button onClick={handleRoomsRedirect}>Play vs Humans</Button>
        </div>
    );
};

export default GameMenu;
