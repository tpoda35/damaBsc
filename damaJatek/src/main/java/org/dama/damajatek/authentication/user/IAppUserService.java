package org.dama.damajatek.authentication.user;

public interface IAppUserService {
    void changePassword(ChangePasswordRequest request);
    AppUser getLoggedInUser();

}
