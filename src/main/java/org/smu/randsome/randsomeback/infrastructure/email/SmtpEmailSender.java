package org.smu.randsome.randsomeback.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;
import org.smu.randsome.randsomeback.domain.auth.service.EmailSender;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final VerificationCodeManager verificationCodeManager;

    @Value("${mail.from.address}")
    private String fromAddress;

    @Value("${mail.from.name:Randsome}")
    private String fromName;

    @Retryable(
            retryFor = MailException.class,
            maxAttempts = 3,
            backoff = @Backoff(delayExpression = "${mail.retry.delay:10000}", multiplier = 2)
    )
    @Override
    public void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, false, StandardCharsets.UTF_8.name()
            );

            helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new CoreException(ErrorType.INVALID_EMAIL_ADDRESS, e);
        }
    }

    @Recover
    public void recover(Exception e, String to, String subject, String body) {
        log.error("[SmtpEmailSender] 이메일 전송 최종 실패 - to: {}, subject: {}", to, subject, e);
        // 발송이 최종 실패하면 사용자는 코드를 받지 못했는데 코드는 유효한 상태로 남아있게 되므로, 코드 무효화 처리
        verificationCodeManager.invalidateVerificationCode(to /*email*/);
        throw new CoreException(ErrorType.EMAIL_SEND_FAILED);
    }

}