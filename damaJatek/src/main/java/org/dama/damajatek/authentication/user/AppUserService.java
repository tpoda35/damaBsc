package org.dama.damajatek.authentication.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.exception.PasswordMismatchException;
import org.dama.damajatek.exception.auth.UserNotLoggedInException;
import org.dama.damajatek.exception.auth.WrongPasswordException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService implements IAppUserService {

    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final IAppUserCacheService appUserCacheService;

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        AppUser user = getLoggedInUser();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new WrongPasswordException();
        }

        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new PasswordMismatchException();
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        appUserRepository.save(user);
    }

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
