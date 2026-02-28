package kz.legeal.ease.backend.service.impl;

import jakarta.mail.MessagingException;
import kz.legeal.ease.backend.dto.EmailMessage;
import kz.legeal.ease.backend.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    @Override
    public void send(EmailMessage message) {
        try {
            final var html = renderTemplate(message);
            sendMimeMessage(message.getTo(), message.getSubject(), html);
            log.info("Email '{}' sent to {}", message.getSubject(), message.getTo());
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", message.getSubject(), message.getTo(), e.getMessage(), e);
        }
    }

    private String renderTemplate(EmailMessage message) {
        final var context = new Context();
        context.setVariables(message.getVariables());
        return templateEngine.process(message.getTemplateName(), context);
    }

    private void sendMimeMessage(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        final var mimeMessage = mailSender.createMimeMessage();
        final var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.setFrom("220107072@stu.sdu.edu.kz", "LegalEase");

        mailSender.send(mimeMessage);
    }
}
