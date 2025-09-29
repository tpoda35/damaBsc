package org.dama.damajatek.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDto;
import org.dama.damajatek.entity.Room;
import org.dama.damajatek.repository.RoomRepository;
import org.dama.damajatek.service.IRoomService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService implements IRoomService {

    private final RoomRepository roomRepository;

    @Override
    public void create(RoomCreateDto roomCreateDto) {

    }

    @Override
    public void join(Long roomId, String password) {

    }

    @Override
    public void leave(Long roomId) {

    }

    @Override
    public void kick(Long userId, Long roomId) {

    }

    @Override
    public Page<RoomInfoDto> getRooms(int PageNum, int PageSize) {
        return null;
    }
}
