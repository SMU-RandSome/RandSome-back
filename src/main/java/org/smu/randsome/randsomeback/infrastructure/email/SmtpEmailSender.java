package org.smu.randsome.randsomeback.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.auth.service.EmailSender;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Retryable(
            retryFor = MailException.class,
            maxAttempts = 3,
            backoff = @Backoff(delayExpression = "${mail.retry.delay:10000}", multiplier = 2)
    )
    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    @Recover
    public void recover(MailException e, String to, String subject) {
        log.error("[SmtpEmailSender] 이메일 전송 최종 실패 - to: {}, subject: {}", to, subject, e);
        throw new CoreException(ErrorType.EMAIL_SEND_FAILED);
    }

}