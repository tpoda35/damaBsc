package org.dama.damajatek.authentication.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dama.damajatek.exception.PasswordMismatchException;
import org.dama.damajatek.exception.auth.UserNotLoggedInException;
import org.dama.damajatek.exception.auth.WrongPasswordException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;

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
}
