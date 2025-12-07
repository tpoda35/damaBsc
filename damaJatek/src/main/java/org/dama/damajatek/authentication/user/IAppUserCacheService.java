package org.dama.damajatek.authentication.user;

import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.entity.Game;

public interface IAppUserCacheService {
    AppUserInfoDto loadProfileInfo(Long userId, AppUser loggedInUser);
    void evictProfileCache(Long userId);
    void evictPlayers(Game game);
}
