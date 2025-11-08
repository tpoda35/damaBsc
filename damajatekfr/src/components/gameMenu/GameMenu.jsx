import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import Button from "../Button.jsx";
import Modal from "../Modal.jsx";
import Form from "../Form.jsx";

const GameMenu = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [error, setError] = useState(null);

    const navigate = useNavigate();

    const handleRedirect = () => {
        navigate("/vsPlayer");
    };

    const handleCreateAIGame = async (formData) => {
        try {
            setIsModalOpen(false);
        } catch (err) {
            setError(err.message || "Failed to create room");
        }
    };

    const fields = [
        { label: "Difficulty", name: "color", type: "select", required: true, options: [
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
                Vs Computer
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

            <Button onClick={handleRedirect}>Vs Player</Button>
        </div>
    );
};

export default GameMenu;
