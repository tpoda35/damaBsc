import { useSharedAuth } from "../contexts/AuthContext";
import GameMenu from "../components/GameMenu.jsx";
import styles from "./Home.module.css";
import Button from "../components/Button.jsx";
import { useNavigate } from "react-router-dom";

import React, { useEffect, useRef } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls";

const Home = () => {
    const mountRef = useRef(null);

    const { user } = useSharedAuth();
    const navigate = useNavigate();

    const handleLoginRedirect = () => {
        navigate("/login");
    };

    const handleRegisterRedirect = () => {
        navigate("/register");
    };

    return (
        <div className={styles.pageWrapper}>
            <div ref={mountRef} className={styles.canvas} />

            <div className={styles.uiOverlay}>
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
        </div>
    );
};

export default Home;
