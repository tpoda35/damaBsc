package org.dama.damajatek.dto.appUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.room.ReadyStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUserGameDto {

    private Long id;
    private String displayName;
    private ReadyStatus readyStatus;

}
