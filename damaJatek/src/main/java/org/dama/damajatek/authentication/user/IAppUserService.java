package org.dama.damajatek.authentication.user;

import org.dama.damajatek.dto.AppUserInfoDto;

import java.util.concurrent.CompletableFuture;

public interface IAppUserService {
    AppUser getLoggedInUser();
    CompletableFuture<AppUserInfoDto> getProfileInfo();
}
