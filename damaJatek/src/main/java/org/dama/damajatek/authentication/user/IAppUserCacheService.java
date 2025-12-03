package org.dama.damajatek.authentication.user;

import org.dama.damajatek.dto.AppUserInfoDto;

public interface IAppUserCacheService {
    AppUserInfoDto loadProfileInfo(Long userId, AppUser loggedInUser);
}
