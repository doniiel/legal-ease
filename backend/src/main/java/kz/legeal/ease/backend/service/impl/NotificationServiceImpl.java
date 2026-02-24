package kz.legeal.ease.backend.service.impl;


import kz.legeal.ease.backend.dto.EmailMessage;
import kz.legeal.ease.backend.service.EmailSenderService;
import kz.legeal.ease.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailSenderService emailSenderService;

    @Override
    public void sendVerificationCode(String email, String code) {
        log.info("Sending verification code to {}", email);

        emailSenderService.send(EmailMessage.builder()
                .to(email)
                .subject("Verify your LegalEase account")
                .templateName("verification-code")
                .variables(Map.of("code", code))
                .build());
    }

    @Override
    public void sendResetPasswordCode(String email, String code) {
        log.info("Sending reset password code to {}", email);

        emailSenderService.send(EmailMessage.builder()
                .to(email)
                .subject("Reset your LegalEase password")
                .templateName("reset-password")
                .variables(Map.of("code", code))
                .build());
    }

    @Override
    public void sendLawyerApproved(String email) {
        log.info("Sending lawyer approved notification to {}", email);

        emailSenderService.send(EmailMessage.builder()
                .to(email)
                .subject("🎉 Your LegalEase application has been approved")
                .templateName("lawyer-approved")
                .variables(Map.of())
                .build());
    }

    @Override
    public void sendLawyerRejected(String email, String reason) {
        log.info("Sending lawyer rejected notification to {}", email);

        emailSenderService.send(EmailMessage.builder()
                .to(email)
                .subject("Update on your LegalEase application")
                .templateName("lawyer-rejected")
                .variables(Map.of("reason", reason))
                .build());
    }

    @Override
    public void sendLawyerRoleRevoked(String email) {
        log.info("Sending lawyer role revoked notification to {}", email);
        emailSenderService.send(EmailMessage.builder()
                .to(email)
                .subject("Update on your LegalEase account")
                .templateName("lawyer-role-revoked")
                .variables(Map.of())
                .build());
    }
}