package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.EmailMessage;

public interface EmailSenderService {

    public void send(EmailMessage message);
}
