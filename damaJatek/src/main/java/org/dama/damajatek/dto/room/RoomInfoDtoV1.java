package org.dama.damajatek.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserInfoDtoV1;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomInfoDtoV1 {

    private Long id;
    private String name;

    private Boolean isHost;
    private AppUserInfoDtoV1 host;
    private AppUserInfoDtoV1 opponent;

}
