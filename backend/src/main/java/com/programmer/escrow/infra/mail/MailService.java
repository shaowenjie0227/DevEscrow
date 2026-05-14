package com.programmer.escrow.infra.mail;

public interface MailService {

    void sendText(String to, String subject, String content);
}
