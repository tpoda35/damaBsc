package org.dama.damajatek.security.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dama.damajatek.exception.PasswordMismatchException;
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

    /**
     * Changes the password of the currently authenticated user.
     * <p>
     * This method verifies that the provided current password matches the
     * authenticated user's existing password. It also ensures that the new
     * password and its confirmation match. If the validations pass, the
     * user's password is updated and persisted in the repository.
     * </p>
     *
     * @param request        the {@link ChangePasswordRequest} containing the current password,
     *                       new password, and confirmation password.
     *
     * @throws WrongPasswordException     if the current password is incorrect.
     * @throws PasswordMismatchException  if the new password and its confirmation do not match.
     */
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

    /**
     * Method, which gives back the current authenticated user with the {@link SecurityContextHolder}.
     *
     * @return a {@link AppUser}.
     */
    public AppUser getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUser) authentication.getPrincipal();
    }
}
