package org.dama.damajatek.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserProfileDto;
import org.dama.damajatek.dto.game.GameHistoryDto;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDto {

    Page<GameHistoryDto> gameHistoryDtoPage;
    AppUserProfileDto appUserProfileDto;

}
