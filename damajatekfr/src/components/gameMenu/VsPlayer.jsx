import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import Button from "../Button.jsx";

const GameMenu = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);

    const navigate = useNavigate();

    const handleRedirect = () => {
        navigate("/rooms");
    };

    return (
        <div>
            <Button>Quick Match</Button>

            {/*<Button*/}
            {/*    onClick={() => setIsModalOpen(true)}*/}
            {/*>*/}
            {/*    Open Settings*/}
            {/*</Button>*/}

            {/*<Modal*/}
            {/*    isOpen={isModalOpen}*/}
            {/*    onClose={() => setIsModalOpen(false)}*/}
            {/*    title="Set up AI game"*/}
            {/*>*/}
            {/*    <Form*/}
            {/*        fields={fields}*/}
            {/*        buttonText="Play"*/}
            {/*    />*/}
            {/*</Modal>*/}

            <Button onClick={handleRedirect}>Rooms</Button>
        </div>
    );
};

export default GameMenu;
