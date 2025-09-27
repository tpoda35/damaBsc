package org.dama.damajatek.security.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

/**
 * Controller class for the user authentication system.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        service.register(request);
        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody @Valid AuthenticationRequest request,
            HttpServletResponse response
    ) {
        AuthenticationResponse authResponse = service.login(request);

        ResponseCookie accessTokenCookie = ResponseCookie.from("Authorization", authResponse.getAccessToken())
                .httpOnly(true)
//                .secure(true) only on https
                .sameSite("Lax")
                .path("/")
                .maxAge(30 * 60) // 30 min
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("RefreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
//                .secure(true) only on https
                .sameSite("Lax")
                .path("/")
                .maxAge(30L * 24 * 60 * 60) // 30 days
                .build();

        response.addHeader(SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(authResponse.getAccessToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(value = "RefreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        String newAccessToken = service.refreshToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("Authorization", newAccessToken)
                .httpOnly(true)
//                .secure(true) only on https
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(SET_COOKIE, accessTokenCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
