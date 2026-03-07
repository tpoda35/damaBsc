import React, { useRef, useState, useLayoutEffect } from "react";
import BoardCell from "./BoardCell";
import Piece from "./Piece";
import { motion, AnimatePresence } from "framer-motion";
import "./GameBoard.css";

const BOARD_SIZE = 8;

const GameBoard = ({
                       board,
                       allowedMoves,
                       selectedCell,
                       onCellClick,
                       playerColor,
                   }) => {
    const boardRef = useRef(null);
    const [cellSize, setCellSize] = useState(null);

    // Measure the inner cell size, excluding the boards border
    // Its needed, because of the responsive design
    // Im used this, because useEffect async was buggy
    useLayoutEffect(() => {
        const measure = () => {
            if (boardRef.current) {
                const style = window.getComputedStyle(boardRef.current);
                const borderLeft = parseFloat(style.borderLeftWidth) || 0;
                const borderRight = parseFloat(style.borderRightWidth) || 0;
                const innerWidth = boardRef.current.getBoundingClientRect().width - borderLeft - borderRight;
                setCellSize(innerWidth / BOARD_SIZE);
            }
        };
        measure();
        const ro = new ResizeObserver(measure);
        if (boardRef.current) ro.observe(boardRef.current);
        return () => ro.disconnect();
    }, []);

    const movesForSelected = selectedCell
        ? allowedMoves.filter(
            (m) =>
                m.fromRow === selectedCell.row &&
                m.fromCol === selectedCell.col
        )
        : [];

    const isHighlighted = (row, col) =>
        movesForSelected.some((m) => m.toRow === row && m.toCol === col);

    // Collect all pieces with their positions for the overlay layer
    const pieces = [];
    board.grid.forEach((rowData, rowIndex) => {
        rowData.forEach((piece, colIndex) => {
            if (piece) pieces.push({ piece, rowIndex, colIndex });
        });
    });

    // Rotate the board
    const shouldRotate = playerColor.toLowerCase() !== "white";

    // Rotates the board if needed
    const toVisualPos = (row, col) => {
        if (shouldRotate) {
            return { vRow: BOARD_SIZE - 1 - row, vCol: BOARD_SIZE - 1 - col };
        }
        return { vRow: row, vCol: col };
    };

    return (
        <div className="board-wrapper">
            {/* Grid of cells are purely visual + click targets */}
            <div
                ref={boardRef}
                className={`board ${shouldRotate ? "rotated" : ""}`}
            >
                {board.grid.map((rowData, rowIndex) =>
                    rowData.map((_, colIndex) => {
                        const p = board.grid[rowIndex][colIndex];
                        const canSelect =
                            p &&
                            p.color &&
                            p.color.toLowerCase() === playerColor.toLowerCase();

                        return (
                            <BoardCell
                                key={`${rowIndex}-${colIndex}`}
                                row={rowIndex}
                                col={colIndex}
                                piece={null}
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

            {/* Piece overlay */}
            {cellSize && (
                <div className="piece-overlay">
                    <AnimatePresence>
                        {pieces.map(({ piece, rowIndex, colIndex }) => {
                            const { vRow, vCol } = toVisualPos(rowIndex, colIndex);
                            return (
                                <motion.div
                                    key={piece.id}
                                    className="piece-overlay-cell"
                                    initial={false}
                                    animate={{
                                        left: vCol * cellSize,
                                        top: vRow * cellSize,
                                    }}
                                    exit={{ opacity: 0, scale: 0.5 }}
                                    transition={{
                                        type: "spring",
                                        stiffness: 300,
                                        damping: 25,
                                        mass: 0.5,
                                    }}
                                    style={{
                                        width: cellSize,
                                        height: cellSize,
                                        position: "absolute",
                                    }}
                                    onClick={() => onCellClick(rowIndex, colIndex)}
                                >
                                    <Piece
                                        color={piece.color}
                                        isKing={piece.king}
                                        pieceId={piece.id}
                                    />
                                </motion.div>
                            );
                        })}
                    </AnimatePresence>
                </div>
            )}
        </div>
    );
};

export default GameBoard;