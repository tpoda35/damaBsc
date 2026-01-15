import { motion } from "framer-motion";
import styles from "./CheckersHeroAnimation.module.css";

const pieces = [
    { color: "black", style: { top: "20%", left: "5%" }, delay: 0 },
    { color: "red", style: { top: "45%", left: "50%" }, delay: 0.2 },
    { color: "red", style: { top: "10%", left: "40%" }, delay: 0.4 }
];

export default function CheckersHeroAnimation() {
    return (
        <div className={styles.scene}>
            {pieces.map((piece, i) => (
                <motion.div
                    key={i}
                    className={`${styles.piece} ${styles[piece.color]}`}
                    initial={{ opacity: 0, y: -20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{
                        duration: 1,
                        ease: "easeOut",
                        delay: piece.delay
                    }}
                    style={{ ...piece.style, willChange: "transform, opacity" }}
                />
            ))}
        </div>
    );
}
