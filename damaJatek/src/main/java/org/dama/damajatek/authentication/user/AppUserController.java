package org.dama.damajatek.authentication.user;

import lombok.RequiredArgsConstructor;
import org.dama.damajatek.dto.UserInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public UserInfoDto getUserInfo() {
        AppUser appUser = appUserService.getLoggedInUser();

        return UserInfoDto.builder()
                .displayName(appUser.getDisplayName())
                .userId(appUser.getId())
                .createdAt(appUser.getCreatedAt())
                .build();
    }
}
