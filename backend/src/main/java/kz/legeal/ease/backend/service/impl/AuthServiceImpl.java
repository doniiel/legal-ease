package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.AuthResponseDto;
import kz.legeal.ease.backend.enums.Role;
import kz.legeal.ease.backend.enums.VerificationType;
import kz.legeal.ease.backend.exception.auth.InvalidCredentialsException;
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
    private final TokenService tokenService;
    private final NotificationService notificationService;
    private final VerificationService verificationService;
    private final AuthenticationManager authenticationManager;


    @Override
    @Transactional
    public AuthResponseDto login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        final var userDetails = (PersonDetails) personDetailsService
                .loadUserByUsernameWithHighestRole(request.getEmail());

        final var accessToken = tokenService.generateAccessToken(userDetails);
        final var refreshToken = tokenService.generateRefreshToken(userDetails);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenService.getAccessExpMin())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        final var user = userService.createUser(request);
        userRoleService.assignRole(user, Role.USER.name());
        final var code = verificationService.generate(user.getEmail(), VerificationType.REGISTER);
        notificationService.sendVerificationCode(user.getEmail(), code);
    }

    @Override
    @Transactional
    public void confirm(VerificationRequest request) {
        userService.confirmAccount(request.getEmail(), request.getCode());
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequest request) {
        final var storedToken = tokenService.findValidStoredToken(request.getRefreshToken());
        final var userDetails = (PersonDetails) personDetailsService
                .loadUserByUsername(storedToken.getUser().getEmail());

        tokenService.revokeToken(storedToken);

        final var newAccessToken = tokenService.generateAccessToken(userDetails);
        final var newRefreshToken = tokenService.generateRefreshToken(userDetails);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(tokenService.getAccessExpMin())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void sendResetPasswordCode(ResetPasswordRequest request) {
        personDetailsService.loadUserByUsername(request.getEmail());
        final var code = verificationService.generate(request.getEmail(), VerificationType.RESET_PASSWORD);
        notificationService.sendResetPasswordCode(request.getEmail(), code);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        final var user = userService.changePassword(request.getEmail(), request.getCode(), request.getNewPassword());
        tokenService.revokeAllByUser(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        final var storedToken = tokenService.findValidStoredToken(request.getRefreshToken());
        tokenService.revokeToken(storedToken);
    }
}
