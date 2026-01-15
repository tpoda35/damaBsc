import React from "react";
import Piece from "./Piece";
import { AnimatePresence } from "framer-motion";
import "./GameBoard.css";

const BoardCell = ({
                       row,
                       col,
                       piece,
                       onClick,
                       isSelected,
                       isHighlighted,
                       canSelect,
                   }) => {
    console.log("ReRender BoardCell");

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
            onClick={() => onClick(row, col)}
        >
            <AnimatePresence>
                {piece && (
                    <Piece
                        key={`piece-${piece.id}`} // Use piece.id as key, not position
                        color={piece.color}
                        isKing={piece.king}
                        pieceId={piece.id} // Pass the unique piece ID
                    />
                )}
            </AnimatePresence>
        </div>
    );
};

export default React.memo(BoardCell);