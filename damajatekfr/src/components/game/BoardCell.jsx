import React from "react";
import Piece from "./Piece";
import "../../styles/GameBoard.css";

const BoardCell = ({
                       row,
                       col,
                       piece,
                       onClick,
                       isSelected,
                       isHighlighted,
                       canSelect,
                   }) => {
    const isDark = (row + col) % 2 === 1;

    const cellClass = `
        cell 
        ${isDark ? "dark" : "light"}
        ${isHighlighted ? "highlighted" : ""}
        ${isSelected ? "selected" : ""}
        ${canSelect ? "can-select" : ""}
    `;

    return (
        <div
            className={cellClass}
            onClick={() => onClick(row, col)} // always call onClick
        >
            {piece && <Piece color={piece.color} isKing={piece.king} />}
        </div>
    );
};

export default BoardCell;
