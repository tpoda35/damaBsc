import React from "react";
import "../../styles/GameBoard.css";

const Piece = ({ color, isKing }) => {
    const pieceClass = `piece ${color.toLowerCase()} ${isKing ? "king" : ""}`;
    return <div className={pieceClass}></div>;
};

export default Piece;
