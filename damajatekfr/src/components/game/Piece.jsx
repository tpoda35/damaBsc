import React from "react";
import {motion} from "framer-motion";
import "./GameBoard.css";
import './Piece.css';

const Piece = ({ color, isKing, pieceId }) => {
    const pieceClass = `piece ${color.toLowerCase()} ${isKing ? "king" : ""}`;

    // Use pieceId to track the same piece across moves
    // This ensures smooth sliding even when position changes
    return (
        <motion.div
            layoutId={`piece-${pieceId}`}
            className={pieceClass}
            initial={{ scale: 1, opacity: 0.5 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 1, opacity: 0.5 }}
            transition={{
                layout: {
                    type: "spring",
                    stiffness: 200,
                    damping: 30,
                    mass: 0.8
                }
            }}
        />
    );
};

export default Piece;