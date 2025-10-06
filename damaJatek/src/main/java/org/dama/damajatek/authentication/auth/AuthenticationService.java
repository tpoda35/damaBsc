package org.dama.damajatek.authentication.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dama.damajatek.exception.auth.InvalidRefreshTokenException;
import org.dama.damajatek.exception.auth.RefreshTokenNotFoundException;
import org.dama.damajatek.exception.UserNotFoundException;
import org.dama.damajatek.authentication.config.JwtService;
import org.dama.damajatek.authentication.token.Token;
import org.dama.damajatek.authentication.token.TokenRepository;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.authentication.user.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.dama.damajatek.authentication.token.TokenType.BEARER;
import static org.dama.damajatek.authentication.user.Role.USER;
import static org.dama.damajatek.authentication.user.Status.OFFLINE;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())){
            throw new BadCredentialsException("Email already in use.");
        }

        AppUser user = AppUser.builder()
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(USER)
                .status(OFFLINE)
                .build();

        repository.save(user);
    }

    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        AppUser user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user); // Will be modified to stay the same for 30 days.

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    protected void saveUserToken(AppUser appUser, String jwtToken) {
        var token = Token.builder()
                .appUser(appUser)
                .token(jwtToken)
                .tokenType(BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    @Transactional
    protected void revokeAllUserTokens(AppUser appUser) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(appUser.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Transactional
    public String refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenNotFoundException();
        }

        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            throw new InvalidRefreshTokenException();
        }

        var user = repository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new InvalidRefreshTokenException();
        }

        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return accessToken;
    }
}
