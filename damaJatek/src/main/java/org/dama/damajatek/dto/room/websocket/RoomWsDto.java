package org.dama.damajatek.dto.room.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserInfoDtoV1;
import org.dama.damajatek.enums.room.RoomWsAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomWsDto {

    @Enumerated(EnumType.STRING)
    private RoomWsAction action;

    private AppUserInfoDtoV1 player;

}
