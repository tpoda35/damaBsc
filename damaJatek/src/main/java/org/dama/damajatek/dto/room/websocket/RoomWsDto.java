package org.dama.damajatek.dto.room.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserInfoDtoV1;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.enums.room.RoomWsAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomWsDto {

    // This will be mainly sent to the host

    @Enumerated(EnumType.STRING)
    private RoomWsAction action;

    private Long id;
    private String name;

    private AppUserInfoDtoV1 opponent;

}
