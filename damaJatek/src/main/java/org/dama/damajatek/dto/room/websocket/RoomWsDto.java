package org.dama.damajatek.dto.room.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.annotation.room.ValidRoomWsDto;
import org.dama.damajatek.dto.room.RoomCreateDto;
import org.dama.damajatek.dto.room.RoomInfoDtoV1;
import org.dama.damajatek.enums.room.RoomWsType;

@ValidRoomWsDto // When type is CREATE, roomCreateDto is required, when type is not create, roomId required
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomWsDto {

    @NotNull(message = "Type is required.")
    @Enumerated(EnumType.STRING)
    private RoomWsType type;

    private Long roomId;
    private Long password; // only at join, if needed

    private RoomCreateDto roomCreateDto;

    // Only used for sending data to frontend
    private RoomInfoDtoV1 roomInfoDtoV1;
}
