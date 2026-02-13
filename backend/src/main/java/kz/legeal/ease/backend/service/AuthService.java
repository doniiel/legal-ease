package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.AuthResponseDto;
import kz.legeal.ease.backend.request.*;

public interface AuthService {

    AuthResponseDto login(LoginRequest request);

    void register(RegisterRequest request);

    void confirm(VerificationRequest request);

    AuthResponseDto refreshToken(RefreshTokenRequest request);

    void sendResetPasswordCode(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);

}
