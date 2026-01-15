import {useSharedAuth} from "../contexts/AuthContext";
import styles from "./Home.module.css";
import {useNavigate} from "react-router-dom";

import React from "react";
import CheckersHeroAnimation from "../components/CheckersHeroAnimation.jsx";
import {motion} from "framer-motion";

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
                <div className={styles.heroContainer}>
                    <motion.h1
                        className={styles.welcomeText}
                        initial={{ opacity: 0, y: -20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{
                            duration: 1,
                            ease: "easeOut"
                        }}
                        style={{ willChange: "transform, opacity" }}
                    >
                        Welcome!
                        Please log in to play.
                    </motion.h1>

                    <CheckersHeroAnimation />
                </div>
            ) : (
                <div className={styles.heroContainer}>
                    <motion.h1
                        className={styles.welcomeText}
                        initial={{ opacity: 0, y: -20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{
                            duration: 1,
                            ease: "easeOut"
                        }}
                        style={{ willChange: "transform, opacity" }}
                    >
                        Welcome, {user.displayName || "Player"}!
                    </motion.h1>

                    <CheckersHeroAnimation />
                </div>
            )}
        </div>
    );
};

export default Home;
