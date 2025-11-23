package org.dama.damajatek.authentication.user;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.dto.AppUserInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final IAppUserService appUserService;

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(
          @RequestBody ChangePasswordRequest request
    ) {
        appUserService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public CompletableFuture<AppUserInfoDto> getUserInfo() {
        return appUserService.getProfileInfo();
    }
}
