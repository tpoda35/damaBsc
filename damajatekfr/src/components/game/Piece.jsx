import React, { memo } from "react";
import { motion } from "framer-motion";
import styles from "./Piece.module.css";

const Piece = ({ color, isKing, pieceId }) => {
    const pieceClass = [
        styles.piece,
        `piece-${color.toLowerCase()}`,
        isKing && styles.king,
    ].filter(Boolean).join(" ");

    return (
        <motion.div
            layoutId={`piece-${pieceId}`}
            className={pieceClass}
            transition={{
                layout: {
                    type: "spring",
                    stiffness: 300,
                    damping: 25,
                    mass: 0.5
                }
            }}
        />
    );
};

export default memo(Piece);
