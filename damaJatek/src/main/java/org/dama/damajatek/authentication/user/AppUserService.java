package org.dama.damajatek.authentication.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserProfileDto;
import org.dama.damajatek.exception.PasswordMismatchException;
import org.dama.damajatek.exception.auth.UserNotLoggedInException;
import org.dama.damajatek.exception.auth.WrongPasswordException;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.repository.IRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AppUserService implements IAppUserService {

    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final IRoomRepository roomRepository;
    private final IGameRepository gameRepository;

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

    @Transactional
    @Async
    @Override
    public CompletableFuture<AppUserProfileDto> getProfileInfo() {
        AppUser loggedInUser = getLoggedInUser();

//        Integer hostedRooms = roomRepository.countByHostId(loggedInUser.getId());
//        Integer joinedRooms = roomRepository.countByOpponentId(loggedInUser.getId());
//        Integer wins = gameRepository.countByWinnerUserId(loggedInUser.getId());
//        Integer loses = gameRepository.countByLoserUserId(loggedInUser.getId());

//        AppUserProfileDto profileDto = AppUserProfileDto.builder()
//                .id(loggedInUser.getId())
//                .displayName(loggedInUser.getDisplayName())
//                .email(loggedInUser.getEmail())
//                .hostedRooms(hostedRooms)
//                .joinedRooms(joinedRooms)
//                .wins(wins)
//                .loses(loses)
//                .createdAt(loggedInUser.getCreatedAt())
//                .updatedAt(loggedInUser.getUpdatedAt())
//                .build();
//
//        return CompletableFuture.completedFuture(profileDto);
        return null;
    }

}
