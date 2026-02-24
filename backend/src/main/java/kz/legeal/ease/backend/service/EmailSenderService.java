package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.EmailMessage;

public interface EmailSenderService {

    void send(EmailMessage message);
}
