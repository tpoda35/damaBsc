import {useSharedAuth} from "../contexts/AuthContext";
import styles from "./Home.module.css";
import {useNavigate} from "react-router-dom";

import React from "react";
import CheckersHeroAnimation from "../components/CheckersHeroAnimation.jsx";
import {motion} from "framer-motion";
import CheckersRulesPanel from "../components/CheckersRulesPanel.jsx";
import AnimatedPage from "../components/AnimatedPage.jsx";

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
        <AnimatedPage className={styles.mainContainer}>
            {!user ? (
                <div className={styles.heroContainer}>
                    <h1>
                        Welcome!
                        Please log in to play.
                    </h1>

                    <CheckersHeroAnimation />
                </div>
            ) : (
                <div className={styles.heroContainer}>
                    <h1 className={styles.welcomeText}>
                        Welcome, {user.displayName || "Player"}!
                    </h1>

                    <div className={styles.contentRow}>
                        <CheckersHeroAnimation />
                        <CheckersRulesPanel />
                    </div>
                </div>
            )}
        </AnimatedPage>
    );
};

export default Home;
