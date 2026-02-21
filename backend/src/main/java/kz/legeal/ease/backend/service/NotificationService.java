package kz.legeal.ease.backend.service;

public interface NotificationService {

    void sendVerificationCode(String email, String code);

    void sendResetPasswordCode(String email, String code);

    void sendLawyerApproved(String email);

    void sendLawyerRejected(String email, String reason);
}
