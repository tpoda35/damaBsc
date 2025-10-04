package org.dama.damajatek.service;

import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.dama.damajatek.entity.Game;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IRoomService {
    void create(RoomCreateDto roomCreateDto);
    void join(Long roomId, String password);
    void leave(Long roomId);
    void kick(Long roomId);

    void ready(Long roomId);
    Game start(Long roomId);

    CompletableFuture<Page<RoomInfoDtoV2>> getRooms(int pageNum, int pageSize);
    CompletableFuture<RoomInfoDtoV1> getRoom(Long roomId);
}
