package org.dama.damajatek.authentication.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.exception.auth.UserNotLoggedInException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService implements IAppUserService {

    private final IAppUserCacheService appUserCacheService;

    public AppUser getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UserNotLoggedInException();
        }

        return (AppUser) authentication.getPrincipal();
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<AppUserInfoDto> getProfileInfo() {
        AppUser loggedInUser = getLoggedInUser();
        Long userId = loggedInUser.getId();

        AppUserInfoDto dto = appUserCacheService.loadProfileInfo(userId, loggedInUser);

        return CompletableFuture.completedFuture(dto);
    }

}
