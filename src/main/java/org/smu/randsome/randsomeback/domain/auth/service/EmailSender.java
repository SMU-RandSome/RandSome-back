package org.smu.randsome.randsomeback.domain.auth.service;

public interface EmailSender {

    void send(String to, String subject, String body);

}