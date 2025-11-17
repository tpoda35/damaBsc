package org.dama.damajatek.authentication.user;

import org.dama.damajatek.dto.appUser.AppUserProfileDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IAppUserService {
    void changePassword(ChangePasswordRequest request);
    AppUser getLoggedInUser();
    CompletableFuture<AppUserProfileDto> getProfileInfo();
}
