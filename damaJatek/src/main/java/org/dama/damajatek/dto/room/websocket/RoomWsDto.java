package org.dama.damajatek.dto.room.websocket;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserGameDto;
import org.dama.damajatek.enums.room.RoomWsAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomWsDto {

    @Enumerated(EnumType.STRING)
    @NotNull
    private RoomWsAction action;

    private AppUserGameDto player;

    private Long gameId;

}
