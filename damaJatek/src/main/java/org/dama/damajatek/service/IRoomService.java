package org.dama.damajatek.service;

import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV2;
import org.springframework.data.domain.Page;

public interface IRoomService {
    RoomInfoDtoV1 create(RoomCreateDto roomCreateDto);
    void join(Long roomId, String password);
    void leave(Long roomId);
    void kick(Long roomId);

    void ready(Long roomId);
    void start(Long roomId);

    Page<RoomInfoDtoV2> getRooms(int pageNum, int pageSize);
}
