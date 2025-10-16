package org.dama.damajatek.service;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.enums.room.ReadyStatus;
import org.dama.damajatek.enums.room.RoomWsAction;

public interface IRoomWebSocketService {
    void broadcastRoomUpdate(ReadyStatus readyStatus, AppUser player, RoomWsAction action, String destination);
    void broadcastRoomUpdate(RoomWsAction action, String destination);
    void broadcastRoomUpdate(RoomWsAction action, Long gameId, String destination);
}
