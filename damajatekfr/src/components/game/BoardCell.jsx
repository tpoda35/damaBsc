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
                        key={`piece-${piece.id}`}
                        color={piece.color}
                        isKing={piece.king}
                        pieceId={piece.id}
                    />
                )}
            </AnimatePresence>
        </div>
    );
};

export default React.memo(BoardCell);