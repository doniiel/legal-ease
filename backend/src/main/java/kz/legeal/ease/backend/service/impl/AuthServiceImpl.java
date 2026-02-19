package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.AuthResponseDto;
import kz.legeal.ease.backend.enums.Role;
import kz.legeal.ease.backend.jwt.PersonDetails;
import kz.legeal.ease.backend.jwt.PersonDetailsService;
import kz.legeal.ease.backend.request.*;
import kz.legeal.ease.backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PersonDetailsService personDetailsService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final NotificationService emailService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


    @Override
    @Transactional
    public AuthResponseDto login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());
        try {
            final var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            authenticationManager.authenticate(authToken);

            final var userDetails = (PersonDetails) personDetailsService.loadUserByUsername(request.getEmail());
            final var accessToken = tokenService.generateAccessToken(userDetails);
            final var refreshToken = tokenService.generateRefreshToken(userDetails);

            log.info("Login successful for email: {}", request.getEmail());
            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(tokenService.getAccessExpMin())
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Invalid login attempt for username: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        final var user = userService.createUser(request);
        userRoleService.assignRole(user, Role.USER.name());
        emailService.sendVerificationCode(user.getEmail());
    }

    @Override
    @Transactional
    public void confirm(VerificationRequest request) {
        userService.confirmAccount();
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void sendResetPasswordCode(ResetPasswordRequest request) {

    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {

    }
}
