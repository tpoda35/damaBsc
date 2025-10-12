package org.dama.damajatek.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.dto.room.RoomJoinRequest;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.service.IRoomService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/rooms")
@Slf4j
@RequiredArgsConstructor
public class RoomController {

    private final IRoomService roomService;

    @PostMapping
    public ResponseEntity<Long> createRoom(@RequestBody @Valid RoomCreateDto dto) {
        Long roomId = roomService.create(dto);
        URI location = URI.create("/rooms/" + roomId);
        return ResponseEntity.created(location).body(roomId);
    }

    @PostMapping("/{roomId}/join")
    public Long joinRoom(
            @PathVariable Long roomId,
            @RequestBody(required = false) RoomJoinRequest roomJoinRequest
    ) {
        String password = roomJoinRequest != null ? roomJoinRequest.getPassword() : null;
        return roomService.join(roomId, password);
    }

    @PostMapping("/{roomId}/leave")
    public void leaveRoom(@PathVariable Long roomId) {
        roomService.leave(roomId);
    }

    @PostMapping("/{roomId}/kick")
    public void kickOpponent(@PathVariable Long roomId) {
        roomService.kick(roomId);
    }

    @PostMapping("/{roomId}/ready")
    public void toggleReady(@PathVariable Long roomId) {
        roomService.ready(roomId);
    }

    @PostMapping("/{roomId}/start")
    public Game startGame(@PathVariable Long roomId) {
        return roomService.start(roomId);
    }

    @GetMapping
    public CompletableFuture<Page<RoomInfoDtoV2>> getRooms(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return roomService.getRooms(pageNum, pageSize);
    }

    @GetMapping("/{roomId}")
    public CompletableFuture<RoomInfoDtoV1> getRoom(
            @PathVariable Long roomId
    ) {
        return roomService.getRoom(roomId);
    }
}
