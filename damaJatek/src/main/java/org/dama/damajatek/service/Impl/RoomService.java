package org.dama.damajatek.service.Impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.enums.game.BotDifficulty;
import org.dama.damajatek.exception.PasswordMismatchException;
import org.dama.damajatek.exception.auth.AccessDeniedException;
import org.dama.damajatek.exception.room.*;
import org.dama.damajatek.mapper.RoomMapper;
import org.dama.damajatek.repository.RoomRepository;
import org.dama.damajatek.security.user.AppUser;
import org.dama.damajatek.security.user.AppUserService;
import org.dama.damajatek.service.IGameService;
import org.dama.damajatek.service.IRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

import static org.dama.damajatek.enums.room.ReadyStatus.NOT_READY;
import static org.dama.damajatek.enums.room.ReadyStatus.READY;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService implements IRoomService {

    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserService appUserService;
    private final IGameService gameService;

    @Transactional
    @Override
    public RoomInfoDtoV1 create(RoomCreateDto roomCreateDto) {
        AppUser host = appUserService.getLoggedInUser();

        Room room = Room.builder()
                .name(roomCreateDto.getName())
                .locked(roomCreateDto.isLocked())
                .password(roomCreateDto.isLocked()
                        ? passwordEncoder.encode(roomCreateDto.getPassword())
                        : null)
                .host(host)
                .build();

        return RoomMapper.createRoomInfoDtoV1(roomRepository.save(room));
    }

    @Transactional
    @Override
    public void join(Long roomId, String password) {
        try {
            Room room = findRoomByIdWithUsers(roomId);

            if (room.getOpponent() != null) {
                log.warn("User tried to connect to a full room(id: {}).", roomId);
                throw new RoomAlreadyFullException();
            }

            if (room.isLocked() && !passwordEncoder.matches(password, room.getPassword())) {
                log.warn("Wrong password for room(id: {}).", roomId);
                throw new PasswordMismatchException();
            }

            AppUser opponent = appUserService.getLoggedInUser();

            if (room.getHost().equals(opponent)) {
                log.warn("Host tried to join as opponent in their own room(id: {}).", roomId);
                throw new HostCannotJoinOwnRoomException();
            }

            room.setOpponent(opponent);

        } catch (OptimisticLockException e) {
            log.warn("Concurrent join detected for room(id: {}).", roomId);
            throw new RoomAlreadyFullException();
        }
    }

    @Transactional
    @Override
    public void leave(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser user = appUserService.getLoggedInUser();

        if (user.getId().equals(room.getHost().getId())) {
            log.info("Host left the room(id: {}), deleting room", roomId);
            // Event listener here, to disconnect the opponent from the room.
            roomRepository.delete(room);
        } else if (room.getOpponent() != null && user.getId().equals(room.getOpponent().getId())) {
            room.setOpponent(null);
            // Some kind of message that opponent leaved the room with websocket.
            log.info("Opponent left the room(id: {})", roomId);
        } else {
            log.warn("Unauthorized access to room(id: {}) from user(id: {}).", roomId, user.getId());
            throw new AccessDeniedException("You are not a participant in this room");
        }
    }

    @Transactional
    @Override
    public void kick(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser user = appUserService.getLoggedInUser();

        if (!room.getHost().getId().equals(user.getId())) {
            log.warn("Unauthorized access to room(id: {}) from user(id: {}).", roomId, user.getId());
            throw new AccessDeniedException("Only the host can kick players");
        } else {
            if (room.getOpponent() == null) {
                log.info("Host tried to kick from room(id: {}), but no opponent present.", roomId);
                throw new OpponentNotFoundException("There's no opponent you can kick");
            }

            // Websocket handling.
            room.setOpponent(null);
        }
    }

    @Transactional
    @Override
    public void ready(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser user = appUserService.getLoggedInUser();

        if (room.getHost().getId().equals(user.getId())) {
            // toggle host ready status
            room.setHostReadyStatus(
                    room.getHostReadyStatus() == READY ? NOT_READY : READY
            );
        } else if (room.getOpponent() != null && room.getOpponent().getId().equals(user.getId())) {
            // toggle opponent ready status
            room.setOpponentReadyStatus(
                    room.getOpponentReadyStatus() == READY ? NOT_READY : READY
            );
        } else {
            log.warn("User(id: {}) is not a participant of room(id: {})", user.getId(), roomId);
            throw new AccessDeniedException("You are not a participant in this room");
        }
    }


    @Override
    public void start(Long roomId) {
        Room room = findRoomByIdWithUsers(roomId);

        AppUser user = appUserService.getLoggedInUser();

        if (!user.getId().equals(room.getHost().getId())) {
            log.info("User(id: {}) attempted to access Room(id: {}) but is not the host.", user.getId(), roomId);
            throw new AccessDeniedException("Only the host can start the game");
        }

        if (room.getHostReadyStatus() == READY && room.getOpponentReadyStatus() == READY) {
            // Game start logic
            AppUser redPlayer;
            AppUser blackPlayer;

            // This logic will be used temporarily
            Random random = new Random();
            int number = random.nextInt(2) + 1;
            if (number == 1) {
                blackPlayer = room.getHost();
                redPlayer = room.getOpponent();
            } else {
                blackPlayer = room.getOpponent();
                redPlayer = room.getHost();
            }

            gameService.createGame(redPlayer, blackPlayer, false, BotDifficulty.EASY);
        } else {
            log.warn("Game start blocked for room(id: {}): hostReadyStatus={}, opponentReadyStatus={}",
                    roomId, room.getHostReadyStatus(), room.getOpponentReadyStatus());
            throw new PlayersNotReadyException();
        }
    }

    @Override
    public Page<RoomInfoDtoV2> getRooms(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return roomRepository.findAll(pageable)
                .map(RoomMapper::createRoomInfoDtoV2);
    }

    private Room findRoomByIdWithUsers(Long roomId) {
        return roomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> {
                    log.warn("Room(id: {}) not found.", roomId);
                    return new RoomNotFoundException();
                });
    }
}
