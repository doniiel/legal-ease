package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.AuthResponseDto;
import kz.legeal.ease.backend.request.*;
import kz.legeal.ease.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

    }

    @Override
    @Transactional
    public void confirm(VerificationRequest request) {

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
}
