import { motion, AnimatePresence } from "framer-motion";
import { useEffect } from "react";
import styles from "./GameIntro.module.css";

const GameIntro = ({ playerOne, playerTwo, onFinish }) => {
    useEffect(() => {
        const timer = setTimeout(onFinish, 3000);
        return () => clearTimeout(timer);
    }, [onFinish]);

    return (
        <AnimatePresence>
            <motion.div
                className={styles.overlay}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.6 }}
            >
                <motion.h1
                    className={styles.title}
                    initial={{ opacity: 0, y: -40 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.8 }}
                >
                    Game Starts
                </motion.h1>

                <motion.h2
                    className={styles.vsLine}
                    initial={{ opacity: 0, scale: 0.6 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{
                        delay: 0.6,
                        duration: 0.6,
                        ease: "backOut",
                    }}
                >
                    <span className={styles.turnColorWhite}>{playerOne}</span>
                    <span className={styles.vs}>vs</span>
                    <span className={styles.turnColorRed}>{playerTwo}</span>
                </motion.h2>
            </motion.div>
        </AnimatePresence>
    );
};

export default GameIntro;
