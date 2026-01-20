import React, { memo } from "react";
import { motion } from "framer-motion";
import "./GameBoard.css";
import "./Piece.css";

const Piece = ({ color, isKing, pieceId }) => {
    const pieceClass = `piece piece-${color.toLowerCase()} ${
        isKing ? "king" : ""
    }`;

    return (
        <motion.div
            layoutId={`piece-${pieceId}`}
            className={pieceClass}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{
                layout: {
                    type: "spring",
                    stiffness: 200,
                    damping: 30,
                    mass: 0.8,
                },
                opacity: {
                    duration: 0.2,
                },
            }}
        />
    );
};

export default memo(Piece);
