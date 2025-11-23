package org.dama.damajatek.authentication.user;

import org.dama.damajatek.dto.AppUserInfoDto;

import java.util.concurrent.CompletableFuture;

public interface IAppUserService {
    void changePassword(ChangePasswordRequest request);
    AppUser getLoggedInUser();
    CompletableFuture<AppUserInfoDto> getProfileInfo();
}
