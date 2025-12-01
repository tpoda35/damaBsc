import {useSharedAuth} from "../contexts/AuthContext";
import GameMenu from "../components/GameMenu.jsx";
import styles from "./Home.module.css";
import Button from "../components/Button.jsx";
import {useNavigate} from "react-router-dom";

import React from "react";

const Home = () => {
    const { user } = useSharedAuth();
    const navigate = useNavigate();

    const handleLoginRedirect = () => {
        navigate("/login");
    };

    const handleRegisterRedirect = () => {
        navigate("/register");
    };

    return (
        <div className={styles.mainContainer}>

            {!user ? (
                <div className={styles.hero}>
                    <h2>Login or Register to play</h2>
                    <div className={styles.buttons}>
                        <Button onClick={handleLoginRedirect} variant="primary">Login</Button>
                        <Button onClick={handleRegisterRedirect} variant="primary">Register</Button>
                    </div>
                </div>
            ) : (
                <div className={styles.dashboard}>
                    <h2>Welcome back, {user.displayName || "Player"}!</h2>
                    <GameMenu />
                </div>
            )}
        </div>
    );
};

export default Home;
