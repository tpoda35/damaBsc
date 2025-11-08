package org.dama.damajatek.service.Impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.AppUserRepository;
import org.dama.damajatek.authentication.user.IAppUserService;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.entity.player.Player;
import org.dama.damajatek.exception.PasswordMismatchException;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.room.*;
import org.dama.damajatek.mapper.PlayerMapper;
import org.dama.damajatek.mapper.RoomMapper;
import org.dama.damajatek.repository.IRoomRepository;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.service.IRoomService;
import org.dama.damajatek.service.IRoomWebSocketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.dama.damajatek.enums.room.ReadyStatus.NOT_READY;
import static org.dama.damajatek.enums.room.ReadyStatus.READY;
import static org.dama.damajatek.enums.room.RoomWsAction.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService implements IRoomService {

    // Menu:
    // Start -> Vs Computer, Vs Player
        // Vs Computer -> a mini settings modal, and the game instantly loads
        // Vs Player -> Rooms, Quick Match
    // Settings -> Sound...

    private final IRoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAppUserService appUserService;
    private final IGameService gameService;
    private final IRoomWebSocketService roomWebSocketService;
    private final AppUserRepository appUserRepository;

    // Lock handling, also set database restr. with flyway
    @Transactional
    @Override
    public Long create(RoomCreateDto roomCreateDto) {
        AppUser host = appUserService.getLoggedInUser();

        String encodedPassword = roomCreateDto.getLocked()
                ? passwordEncoder.encode(roomCreateDto.getPassword())
                : null;

        Room room = RoomMapper.createRoom(roomCreateDto, host, encodedPassword);
        return roomRepository.save(room).getId();
    }

    // Lock handling, also set database restr. with flyway
    @Transactional
    @Override
    public Long join(Long roomId, String password) {
        try {
            Room room = findRoomByIdWithUsers(roomId);

            if (room.getOpponent() != null) {
                log.warn("User tried to connect to a full room(id: {}).", roomId);
                throw new RoomAlreadyFullException();
            }

            if (room.getLocked() && !passwordEncoder.matches(password, room.getPassword())) {
                log.warn("Wrong password for room(id: {}).", roomId);
                throw new PasswordMismatchException("Wrong password");
            }

            AppUser loggedInUser = appUserService.getLoggedInUser();

            if (isHost(room, loggedInUser)) {
                log.warn("Host tried to join as opponent in their own room(id: {}).", roomId);
                throw new HostCannotJoinOwnRoomException();
            }

            room.setOpponent(loggedInUser);
            roomWebSocketService.broadcastRoomUpdate(
                    room.getOpponentReadyStatus(),
                    loggedInUser,
                    OPPONENT_JOIN,
                    "/topic/rooms/" + roomId
            );

            return roomId;
        } catch (OptimisticLockException e) {
            // This part is to fix a race condition if two user tries to connect at the same time
            log.warn("Concurrent join detected for room(id: {}).", roomId);
            throw new RoomAlreadyFullException();
        }
    }

    @Transactional
    @Override
    public void leave(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser loggedInUser = appUserService.getLoggedInUser();

        if (isHost(room, loggedInUser)) {
            log.info("Host left the room(id: {}), deleting room", roomId);
            if (room.getStarted() != null && !room.getStarted()) roomRepository.delete(room);

            roomWebSocketService.broadcastRoomUpdate(HOST_LEAVE, "/topic/rooms/" + roomId);
        } else if (room.getOpponent() != null && isOpponent(room, loggedInUser)) {
            room.setOpponent(null);
            log.info("Opponent left the room(id: {})", roomId);

            roomWebSocketService.broadcastRoomUpdate(OPPONENT_LEAVE, "/topic/rooms/" + roomId);
        } else {
            log.warn("Unauthorized access to room(id: {}) from user(id: {}).", roomId, loggedInUser.getId());
            throw new AccessDeniedException("You are not a participant in this room");
        }
    }

    @Transactional
    @Override
    public void kick(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser loggedInUser = appUserService.getLoggedInUser();

        if (!isHost(room, loggedInUser)) {
            log.warn("Unauthorized access to room(id: {}) from user(id: {}).", roomId, loggedInUser.getId());
            throw new AccessDeniedException("Only the host can kick players");
        } else {
            if (room.getOpponent() == null) {
                log.info("Host tried to kick from room(id: {}), but no opponent present.", roomId);
                throw new OpponentNotFoundException("There's no opponent you can kick");
            }

            room.setOpponent(null);
            roomWebSocketService.broadcastRoomUpdate(KICK, "/topic/rooms/" + roomId);
        }
    }

    @Transactional
    @Override
    public void ready(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser loggedInUser = appUserService.getLoggedInUser();

        if (isHost(room, loggedInUser)) {
            // toggle host ready status
            room.setHostReadyStatus(
                    room.getHostReadyStatus() == READY ? NOT_READY : READY
            );

            roomWebSocketService.broadcastRoomUpdate(room.getHostReadyStatus(), loggedInUser, HOST_READY, "/topic/rooms/" + roomId);
        } else if (room.getOpponent() != null && isOpponent(room, loggedInUser)) {
            // toggle opponent ready status
            room.setOpponentReadyStatus(
                    room.getOpponentReadyStatus() == READY ? NOT_READY : READY
            );

            roomWebSocketService.broadcastRoomUpdate(room.getOpponentReadyStatus(), loggedInUser, OPPONENT_READY, "/topic/rooms/" + roomId);
        } else {
            log.warn("User(id: {}) is not a participant of room(id: {})", loggedInUser.getId(), roomId);
            throw new AccessDeniedException("You are not a participant in this room");
        }
    }


    @Transactional
    @Override
    public void start(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser loggedInUser = appUserService.getLoggedInUser();

        if (!loggedInUser.getId().equals(room.getHost().getId())) {
            log.info("User(id: {}) attempted to access Room(id: {}) but is not the host.", loggedInUser.getId(), roomId);
            throw new AccessDeniedException("Only the host can start the game");
        }

        if (room.isFullyReady()) {
            // Game start logic
            Player redPlayer;
            Player blackPlayer;

            // This logic will be used temporarily
            Random random = new Random();
            int number = random.nextInt(2) + 1;
            if (number == 1) {
                blackPlayer = PlayerMapper.createHumanPlayer(room.getHost());
                redPlayer = PlayerMapper.createHumanPlayer(room.getOpponent());
            } else {
                blackPlayer = PlayerMapper.createHumanPlayer(room.getOpponent());
                redPlayer = PlayerMapper.createHumanPlayer(room.getHost());
            }

            room.setStarted(true);
            roomRepository.save(room);

            Game game = gameService.createGame(redPlayer, blackPlayer, room);
            roomWebSocketService.broadcastRoomUpdate(START, game.getId(), "/topic/rooms/" + roomId);
        } else {
            log.warn("Game start blocked for room(id: {}): hostReadyStatus={}, opponentReadyStatus={}",
                    roomId, room.getHostReadyStatus(), room.getOpponentReadyStatus());
            throw new PlayersNotReadyException();
        }
    }

    @Async
    @Override
    public CompletableFuture<Page<RoomInfoDtoV2>> getRooms(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<RoomInfoDtoV2> rooms = roomRepository.findAll(pageable)
                .map(RoomMapper::createRoomInfoDtoV2);

        return CompletableFuture.completedFuture(rooms);
    }

    @Async
    @Override
    public CompletableFuture<RoomInfoDtoV1> getRoom(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);
        AppUser loggedInUser = appUserService.getLoggedInUser();

        AppUser host = room.getHost();
        AppUser opponent = room.getOpponent();

        boolean isHost = host != null && loggedInUser.getId().equals(host.getId());
        boolean isOpponent = opponent != null && loggedInUser.getId().equals(opponent.getId());

        if (!isHost && !isOpponent) {
            log.warn("User(id: {}) is not a participant of room(id: {})", loggedInUser.getId(), roomId);
            throw new AccessDeniedException("You are not a participant in this room");
        }

        RoomInfoDtoV1 roomInfoDtoV1 = RoomMapper.createRoomInfoDtoV1(room, host, opponent, isHost);

        return CompletableFuture.completedFuture(
                roomInfoDtoV1
        );
    }

    @Transactional
    @Override
    public void handleUserDisconnect(String email) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElse(null);

        if (appUser == null) {
            log.warn("User not found for disconnect cleanup: {}", email);
            return;
        }

        Room room = roomRepository.findByHostOrOpponent(appUser)
                .orElse(null);

        if (room == null) {
            log.debug("User {} not in any room, no cleanup needed", email);
            return;
        }

        log.info("Cleaning up room {} for disconnected appUser {}", room.getId(), email);

        boolean isHost = isHost(room, appUser);

        if (isHost) {
            log.info("Host disconnected from room {}, closing room", room.getId());

            if (room.getOpponent() != null) {
                roomWebSocketService.broadcastRoomUpdate(HOST_LEAVE, "/topic/rooms/" + room.getId());
            }

            roomRepository.delete(room);
        } else {
            log.info("Opponent disconnected from room {}, removing opponent", room.getId());

            room.setOpponent(null);
            roomRepository.save(room);

            roomWebSocketService.broadcastRoomUpdate(OPPONENT_LEAVE, "/topic/rooms/" + room.getId());
        }
    }

    private Room findRoomByIdWithUsers(Long roomId) {
        return roomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> {
                    log.warn("Room(id: {}) not found.", roomId);
                    return new RoomNotFoundException();
                });
    }

    private boolean isHost(Room room, AppUser loggedInUser) {
        return room.getHost().getId().equals(loggedInUser.getId());
    }

    private boolean isOpponent(Room room, AppUser loggedInUser) {
        return room.getOpponent().getId().equals(loggedInUser.getId());
    }
}
