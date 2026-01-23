import React from "react";
import BoardCell from "./BoardCell";
import "./GameBoard.css";

const GameBoard = ({
                       board,
                       allowedMoves,
                       selectedCell,
                       onCellClick,
                       playerColor,
                   }) => {
    const movesForSelected = selectedCell
        ? allowedMoves.filter(
            (m) =>
                m.fromRow === selectedCell.row && m.fromCol === selectedCell.col
        )
        : [];

    const isHighlighted = (row, col) =>
        movesForSelected.some((m) => m.toRow === row && m.toCol === col);

    const shouldRotate = playerColor.toLowerCase() === 'white';

    return (
        <div className={`board ${shouldRotate ? '' : 'rotated'}`}>
            {board.grid.map((rowData, rowIndex) =>
                rowData.map((piece, colIndex) => {
                    const canSelect =
                        piece &&
                        piece.color &&
                        piece.color.toLowerCase() === playerColor.toLowerCase();

                    return (
                        <BoardCell
                            key={`${rowIndex}-${colIndex}`}
                            row={rowIndex}
                            col={colIndex}
                            piece={piece}
                            onClick={onCellClick}
                            isSelected={
                                selectedCell &&
                                selectedCell.row === rowIndex &&
                                selectedCell.col === colIndex
                            }
                            isHighlighted={isHighlighted(rowIndex, colIndex)}
                            canSelect={canSelect}
                        />
                    );
                })
            )}
        </div>
    );
};

export default GameBoard;