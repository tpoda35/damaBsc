import React, { memo } from "react";
import styles from "./Piece.module.css";

const Piece = ({ color, isKing, pieceId }) => {
    const pieceClass = [
        styles.piece,
        `piece-${color.toLowerCase()}`,
        isKing && styles.king,
    ].filter(Boolean).join(" ");

    return <div className={pieceClass} />;
};

export default memo(Piece);