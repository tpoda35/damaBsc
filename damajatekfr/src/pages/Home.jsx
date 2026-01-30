import {useSharedAuth} from "../contexts/AuthContext";
import styles from "./Home.module.css";

import React from "react";
import CheckersHeroAnimation from "../components/CheckersHeroAnimation.jsx";
import CheckersRulesPanel from "../components/CheckersRulesPanel.jsx";
import AnimatedPage from "../components/AnimatedPage.jsx";

const Home = () => {
    const { user } = useSharedAuth();

    return (
        <AnimatedPage className={styles.mainContainer}>
            {!user ? (
                <div className={styles.heroContainer}>
                    <h1 className={styles.welcomeText}>
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
