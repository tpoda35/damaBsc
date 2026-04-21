package org.dama.damajatek.authentication.user;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final IAppUserService appUserService;

    @GetMapping
    public CompletableFuture<AppUserInfoDto> getUserInfo() {
        return appUserService.getProfileInfo();
    }
}
