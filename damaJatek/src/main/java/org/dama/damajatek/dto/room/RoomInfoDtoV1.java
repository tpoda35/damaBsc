package org.dama.damajatek.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserGameDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomInfoDtoV1 {

    private Long id;
    private String name;

    private Boolean isHost;
    private AppUserGameDto host;
    private AppUserGameDto opponent;

}
