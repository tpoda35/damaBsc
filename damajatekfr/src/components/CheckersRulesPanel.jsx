import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import styles from "./CheckersRulesPanel.module.css";

const rulesSlides = [
    {
        title: "Objective",
        text: "The goal of the game is to remove all of your opponent's pieces or block them so that they have no legal moves."
    },
    {
        title: "Opening",
        text: "The game starts on an 8×8 board. The red player moves first. Players take turns and can't skip a move."
    },
    {
        title: "Gameplay",
        text: "A move consists of moving one piece diagonally."
    },
    {
        title: "Capturing",
        text: "Capturing is done by jumping over an opponent's piece. Only forward captures are allowed. The square behind the captured piece must be empty. Capturing your own piece or jumping without capturing is not allowed. Capturing is mandatory if available. Multiple captures in one turn are allowed."
    },
    {
        title: "King",
        text: "A piece that reaches the opposite side becomes a king and can move and capture backward."
    },
    {
        title: "End of the Game",
        text: "Win: All opponent pieces are removed. Loss: No legal moves remain. Draw: No capture or promotion for 80 moves, or automatically after 200 moves."
    }
];

const slideVariants = {
    enter: (direction) => ({
        x: direction > 0 ? 50 : -50,
        opacity: 0
    }),
    center: {
        x: 0,
        opacity: 1
    },
    exit: (direction) => ({
        x: direction > 0 ? -50 : 50,
        opacity: 0
    })
};

const CheckersRulesSliderMotion = () => {
    const [index, setIndex] = useState(0);
    const [direction, setDirection] = useState(0);

    const prevSlide = () => {
        setDirection(-1);
        setIndex(i => (i === 0 ? rulesSlides.length - 1 : i - 1));
    };

    const nextSlide = () => {
        setDirection(1);
        setIndex(i => (i === rulesSlides.length - 1 ? 0 : i + 1));
    };

    return (
        <motion.div
            className={styles.sliderContainer}
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 1, ease: "easeOut" }}
            style={{ willChange: "transform, opacity" }}
        >
            <div className={styles.slider}>
                <div className={styles.header}>
                    <h2 className={styles.tutorialTitle}>Tutorial</h2>
                </div>

                <div className={styles.content}>
                    <button className={styles.arrow} onClick={prevSlide}>
                        <i className="fa fa-chevron-left" aria-hidden="true" />
                    </button>

                    <div className={styles.slideWrapper}>
                        <AnimatePresence custom={direction} mode="wait">
                            <motion.div
                                key={index}
                                custom={direction}
                                variants={slideVariants}
                                initial="enter"
                                animate="center"
                                exit="exit"
                                transition={{type: "tween", duration: 0.25, ease: "easeOut"}}
                                className={styles.slide}
                            >
                                <h3 className={styles.slideTitle}>{rulesSlides[index].title}</h3>
                                <p className={styles.slideText}>{rulesSlides[index].text}</p>
                            </motion.div>
                        </AnimatePresence>
                    </div>

                    <button className={styles.arrow} onClick={nextSlide}>
                        <i className="fa fa-chevron-right" aria-hidden="true" />
                    </button>
                </div>
            </div>
        </motion.div>
    );
};

export default CheckersRulesSliderMotion;