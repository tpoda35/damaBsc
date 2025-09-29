package org.dama.damajatek.service;

import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDto;
import org.springframework.data.domain.Page;

public interface IRoomService {
    void create(RoomCreateDto roomCreateDto);
    void join(Long roomId, String password);
    void leave(Long roomId);
    void kick(Long userId, Long roomId);

    Page<RoomInfoDto> getRooms(int PageNum, int PageSize);
}
