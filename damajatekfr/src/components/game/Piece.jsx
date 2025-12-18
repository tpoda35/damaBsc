import React from "react";
import "./GameBoard.css";

const Piece = ({ color, isKing }) => {
    const pieceClass = `piece ${color.toLowerCase()} ${isKing ? "king" : ""}`;
    return <div className={pieceClass}></div>;
};

export default Piece;
