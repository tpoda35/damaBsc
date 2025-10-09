package org.dama.damajatek.service;

import org.dama.damajatek.enums.room.RoomWsAction;

public interface IRoomWebSocketService {
    void broadcastRoomUpdate(Long roomId, RoomWsAction action);
}
