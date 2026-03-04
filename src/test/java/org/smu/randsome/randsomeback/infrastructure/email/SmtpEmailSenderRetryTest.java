package org.smu.randsome.randsomeback.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.auth.service.EmailSender;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = "mail.retry.delay=10")
@RequiredArgsConstructor
class SmtpEmailSenderRetryTest extends IntegrationTestSupport {

    final EmailSender emailSender;

    @MockitoBean
    JavaMailSender javaMailSender;

    @Test
    void 전송_실패_시_최대_3번_재시도한다() {
        // given
        doThrow(new MailSendException("SMTP 오류"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThatThrownBy(() -> emailSender.send("test@sangmyung.kr", "제목", "본문"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.EMAIL_SEND_FAILED);

        verify(javaMailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void 첫_번째_시도에_성공하면_재시도하지_않는다() {
        // given - javaMailSender.send() 정상 동작 (아무것도 throw 하지 않음)

        // when
        emailSender.send("test@sangmyung.kr", "제목", "본문");

        // then
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void 두_번째_시도에_성공하면_1번만_재시도한다() {
        // given
        doThrow(new MailSendException("SMTP 오류"))
                .doNothing()
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        // when
        emailSender.send("test@sangmyung.kr", "제목", "본문");

        // then
        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }

}