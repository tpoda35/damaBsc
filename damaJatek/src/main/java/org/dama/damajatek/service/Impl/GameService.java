package org.dama.damajatek.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.game.GameInfoDtoV1;
import org.dama.damajatek.dto.game.websocket.GameEventDto;
import org.dama.damajatek.dto.game.websocket.MoveMadeEventDto;
import org.dama.damajatek.dto.game.websocket.NextTurnEventDto;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.enums.game.PieceColor;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.game.GameAlreadyFinishedException;
import org.dama.damajatek.exception.game.GameNotFoundException;
import org.dama.damajatek.exception.game.InvalidMoveException;
import org.dama.damajatek.mapper.EventMapper;
import org.dama.damajatek.mapper.GameMapper;
import org.dama.damajatek.model.Board;
import org.dama.damajatek.model.Move;
import org.dama.damajatek.model.Piece;
import org.dama.damajatek.repository.GameRepository;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.util.BoardInitializer;
import org.dama.damajatek.util.BoardSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.dama.damajatek.enums.game.GameStatus.FINISHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final GameRepository gameRepository;
    private final IAppUserService appUserService;

    // The forced capture rule is used, so if there's a capture, then the user only gets that move.

    @Transactional
    @Override
    public Game createGame(AppUser redPlayer, AppUser blackPlayer, Room room, boolean vsBot, BotDifficulty difficulty) {
        Board board = BoardInitializer.createStartingBoard();

        Game game = Game.builder()
                .room(room)
                .redPlayer(redPlayer)
                .blackPlayer(blackPlayer)
                .vsBot(vsBot)
                .botDifficulty(difficulty)
                .build();

        BoardSerializer.saveBoard(game, board);
        return gameRepository.save(game);
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<GameInfoDtoV1> getGameInfo(Long gameId) {
        Game game = findGameByIdWithPlayers(gameId);
        AppUser loggedInUser = appUserService.getLoggedInUser();

        checkUserAccessToGame(game, loggedInUser);

        PieceColor playerColor =
                loggedInUser.getId().equals(game.getRedPlayer().getId())
                        ? PieceColor.RED
                        : PieceColor.BLACK;

        Board board = BoardSerializer.loadBoard(game);
        List<Move> validMoves = getAvailableMoves(board, game.getCurrentTurn());

        return CompletableFuture.completedFuture(
                GameMapper.createGameInfoDtoV1(game, board, validMoves, playerColor)
        );
    }

    @Transactional
    @Override
    public List<GameEventDto> makeMove(Long gameId, Move move, Principal principal) {
        Game game = findGameByIdWithPlayers(gameId);

        Authentication auth = (Authentication) principal;
        AppUser loggedInUser = (AppUser) auth.getPrincipal();

        checkUserAccessToGame(game, loggedInUser);

        if (game.getStatus() == FINISHED) {
            throw new GameAlreadyFinishedException();
        }

        Board board = BoardSerializer.loadBoard(game);
        PieceColor currentTurn = game.getCurrentTurn();

        if ((currentTurn == PieceColor.RED && !loggedInUser.getId().equals(game.getRedPlayer().getId())) ||
                (currentTurn == PieceColor.BLACK && !loggedInUser.getId().equals(game.getBlackPlayer().getId()))) {
            throw new AccessDeniedException("Not your turn");
        }

        // Get available moves (forced capture rule applied)
        List<Move> validMoves = getAvailableMoves(board, currentTurn);

        Move actualMove = validMoves.stream()
                .filter(m -> m.getFromRow() == move.getFromRow() &&
                        m.getFromCol() == move.getFromCol() &&
                        m.getToRow() == move.getToRow() &&
                        m.getToCol() == move.getToCol())
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Move not in valid moves list for game {}: {}", gameId, move);
                    return new InvalidMoveException("Invalid move - forced capture rule violation or move not available");
                });

        applyMove(board, actualMove);
        promoteIfKing(board, actualMove);

        // Switch turns
        PieceColor nextTurn = (currentTurn == PieceColor.RED)
                ? PieceColor.BLACK
                : PieceColor.RED;

        if (isGameOver(board, nextTurn)) {
            game.setStatus(FINISHED);
            game.setWinner(
                    game.getCurrentTurn() == PieceColor.RED ? game.getRedPlayer() : game.getBlackPlayer()
            );
            log.info("Game {} finished. Winner: {}", gameId, currentTurn);
        } else {
            game.setCurrentTurn(nextTurn);
        }

        BoardSerializer.saveBoard(game, board);

        log.info("Move executed in game {}", gameId);
        Game savedGame = gameRepository.save(game);

        List<GameEventDto> events = new ArrayList<>();

        if (!actualMove.getCapturedPieces().isEmpty()) {
            GameEventDto captureEvent = EventMapper.createCaptureMadeEventDto(actualMove);
            events.add(captureEvent);
        } else {
            MoveMadeEventDto moveMadeEvent = EventMapper.createMoveMadeEventDto(actualMove);
            events.add(moveMadeEvent);
        }

        NextTurnEventDto nextTurnEvent = EventMapper.createNextTurnEventDto(
                savedGame.getCurrentTurn(),
                getAvailableMoves(board, nextTurn)
        );
        events.add(nextTurnEvent);

        return events;
    }

    private void checkUserAccessToGame(Game game, AppUser loggedInUser) {
        Long userId = loggedInUser.getId();

        boolean isBlackPlayer = game.getBlackPlayer() != null &&
                userId.equals(game.getBlackPlayer().getId());
        boolean isRedPlayer = game.getRedPlayer() != null &&
                userId.equals(game.getRedPlayer().getId());

        if (!isBlackPlayer && !isRedPlayer) {
            log.warn("Unauthorized access to game(id: {}) from user(id: {}).", game.getId(), userId);
            throw new AccessDeniedException("You are not a participant in this game");
        }
    }





    private List<Move> getAvailableMoves(Board board, PieceColor color) {
        List<Move> captureMoves = findAllCaptureMoves(board, color);

        if (!captureMoves.isEmpty()) {
            return captureMoves;
        }

        return findAllValidMoves(board, color);
    }

    // Iterates through the full table, searches for the pieces with the given color.
    // It uses the findValidMovesForPiece method, to find all the possible moves for all the pieces.
    // It only returns valid moves, not captures.
    private List<Move> findAllValidMoves(Board board, PieceColor color) {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    moves.addAll(findValidMovesForPiece(board, row, col));
                }
            }
        }

        return moves;
    }

    private List<Move> findValidMovesForPiece(Board board, int row, int col) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);

        if (piece == null) return moves;

        // Check regular moves
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            // Skip backward moves for regular pieces
            if (isDirectionAllowed(piece, dir)) continue;

            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(
                        Move.builder()
                        .fromRow(row)
                        .fromCol(col)
                        .toRow(newRow)
                        .toCol(newCol)
                        .build()
                );
            }
        }

        return moves;
    }

    private List<Move> findAllCaptureMoves(Board board, PieceColor color) {
        List<Move> captures = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    captures.addAll(findCaptureMovesForPiece(board, row, col));
                }
            }
        }

        return captures;
    }

    private List<Move> findCaptureMovesForPiece(Board board, int row, int col) {
        List<Move> allCaptures = new ArrayList<>();
        Piece piece = board.getPiece(row, col);

        if (piece == null) return allCaptures;

        Move initialMove = Move.builder()
                .fromRow(row)
                .fromCol(col)
                .toRow(row)
                .toCol(col)
                .build();

        // Start the recursive chain capture search
        findChainCapturesRecursive(board, row, col, piece, initialMove, new HashSet<>(), allCaptures);

        // If no chain captures found, return empty list
        if (allCaptures.isEmpty()) {
            return allCaptures;
        }

        // Find the maximum number of captures in any chain
        int maxCaptures = allCaptures.stream()
                .mapToInt(move -> move.getCapturedPieces().size())
                .max()
                .orElse(0);

        // Only return moves with the maximum number of captures (forced capture rule)
        return allCaptures.stream()
                .filter(move -> move.getCapturedPieces().size() == maxCaptures)
                .collect(Collectors.toList());
    }

    private void findChainCapturesRecursive(
            Board board,
            int currentRow,
            int currentCol,
            Piece originalPiece,
            Move currentMove,
            Set<String> capturedPositions,
            List<Move> allCaptures) {

        boolean foundCapture = false;
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            // Skip backward moves for regular pieces
            if (isDirectionAllowed(originalPiece, dir)) continue;

            int middleRow = currentRow + dir[0];
            int middleCol = currentCol + dir[1];
            int jumpRow = currentRow + dir[0] * 2;
            int jumpCol = currentCol + dir[1] * 2;

            String middleKey = middleRow + "," + middleCol;

            // Check if this is a valid capture
            if (isInBounds(jumpRow, jumpCol) &&
                    isInBounds(middleRow, middleCol) &&
                    board.getPiece(jumpRow, jumpCol) == null &&
                    !capturedPositions.contains(middleKey)) {

                Piece middle = board.getPiece(middleRow, middleCol);

                if (middle != null && middle.getColor() != originalPiece.getColor()) {
                    foundCapture = true;

                    // Create a new move with this capture added
                    Move newMove = Move.builder()
                            .fromRow(currentMove.getFromRow())
                            .fromCol(currentMove.getFromCol())
                            .toRow(jumpRow)
                            .toCol(jumpCol)
                            .build();

                    // Copy all previously captured pieces
                    for (int[] captured : currentMove.getCapturedPieces()) {
                        newMove.getCapturedPieces().add(new int[]{captured[0], captured[1]});
                    }

                    // Add this new captured piece
                    newMove.getCapturedPieces().add(new int[]{middleRow, middleCol});

                    // Copy previous path
                    for (int[] step : currentMove.getPath()) {
                        newMove.getPath().add(new int[]{step[0], step[1]});
                    }

                    newMove.getPath().add(new int[]{jumpRow, jumpCol});

                    // Create new set of captured positions for this branch
                    Set<String> newCaptured = new HashSet<>(capturedPositions);
                    newCaptured.add(middleKey);

                    // Continue searching for more captures from the landing position
                    findChainCapturesRecursive(
                            board,
                            jumpRow,
                            jumpCol,
                            originalPiece,
                            newMove,
                            newCaptured,
                            allCaptures
                    );
                }
            }
        }

        // If no more captures possible from this position, add this as a complete chain
        if (!foundCapture && !currentMove.getCapturedPieces().isEmpty()) {

            // Remove the last element, since we have it in the Move (the toRow, toCol part)
            List<int[]> path = currentMove.getPath();
            if (!path.isEmpty()) {
                int[] last = path.getLast();
                if (last[0] == currentMove.getToRow() && last[1] == currentMove.getToCol()) {
                    path.removeLast();
                }
            }

            allCaptures.add(currentMove);
        }
    }




    // Checks if the given row&col is on the board.
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private void applyMove(Board board, Move move) {
        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());
        board.setPiece(move.getToRow(), move.getToCol(), piece);
        board.removePiece(move.getFromRow(), move.getFromCol());

        // Remove captured pieces
        for (int[] captured : move.getCapturedPieces()) {
            board.removePiece(captured[0], captured[1]);
            log.info("Captured piece at [{}, {}]", captured[0], captured[1]);
        }
    }

    private void promoteIfKing(Board board, Move move) {
        Piece piece = board.getPiece(move.getToRow(), move.getToCol());

        if (piece == null || piece.isKing()) return;

        boolean shouldPromote = false;

        if (piece.getColor() == PieceColor.RED && move.getToRow() == 7) {
            shouldPromote = true;
        } else if (piece.getColor() == PieceColor.BLACK && move.getToRow() == 0) {
            shouldPromote = true;
        }

        if (shouldPromote) {
            piece.setKing(true);
            log.debug("Promoted {} piece to king at [{}, {}]",
                    piece.getColor(), move.getToRow(), move.getToCol());
        }
    }

    public static boolean isDirectionAllowed(Piece piece, int[] dir) {
        // Kings can move in any direction
        if (piece.isKing()) return false;

        // Regular red pieces move "down" (positive row direction)
        if (piece.getColor() == PieceColor.RED && dir[0] > 0) return false;

        // Regular black pieces move "up" (negative row direction)
        return piece.getColor() != PieceColor.BLACK || dir[0] >= 0;
    }

    private boolean isGameOver(Board board, PieceColor currentTurn) {
        // Check if current player has any pieces left
        boolean hasPieces = false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == currentTurn) {
                    hasPieces = true;
                    break;
                }
            }
            if (hasPieces) break;
        }

        if (!hasPieces) {
            log.info("Game over: {} has no pieces left", currentTurn);
            return true;
        }

        // Check if current player has any valid moves
        List<Move> validMoves = getAvailableMoves(board, currentTurn);
        if (validMoves.isEmpty()) {
            log.info("Game over: {} has no valid moves", currentTurn);
            return true;
        }

        return false;
    }

    private Game findGameByIdWithPlayers(Long gameId) {
        return gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> {
                   log.info("Game not found with id {}.", gameId);
                   return new GameNotFoundException();
                });
    }
}