package org.dama.damajatek.dto.room.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.enums.room.RoomWsType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomResponseWsDto {

    @Enumerated(EnumType.STRING)
    private RoomWsType type;

    private RoomInfoDtoV1 roomInfoDtoV1;

}
