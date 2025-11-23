package org.dama.damajatek.authentication.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.dama.damajatek.dto.appUser.AppUserGameStats;
import org.dama.damajatek.exception.PasswordMismatchException;
import org.dama.damajatek.exception.auth.UserNotLoggedInException;
import org.dama.damajatek.exception.auth.WrongPasswordException;
import org.dama.damajatek.mapper.AppUserMapper;
import org.dama.damajatek.repository.IGameRepository;
import org.dama.damajatek.repository.IRoomRepository;
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
    public CompletableFuture<AppUserInfoDto> getProfileInfo() {
        AppUser loggedInUser = getLoggedInUser();
        Long userId = loggedInUser.getId();

        int hostedRooms = roomRepository.countByHostId(userId);
        int joinedRooms = roomRepository.countByOpponentId(userId);

        int vsAiWins = gameRepository.countWinsVsAI(userId);
        int vsAiLoses = gameRepository.countLossesVsAI(userId);

        int vsPlayerWins = gameRepository.countWinsVsPlayer(userId);
        int vsPlayerLoses = gameRepository.countLossesVsPlayer(userId);

        AppUserGameStats gameStats = new AppUserGameStats(
                hostedRooms, joinedRooms, vsAiWins, vsAiLoses, vsPlayerWins, vsPlayerLoses
        );

        AppUserInfoDto appUserInfoDto = AppUserMapper.createAppUserInfoDto(loggedInUser, gameStats);
        log.info("Kutya: {}", appUserInfoDto);
        return CompletableFuture.completedFuture(appUserInfoDto);
    }

}
