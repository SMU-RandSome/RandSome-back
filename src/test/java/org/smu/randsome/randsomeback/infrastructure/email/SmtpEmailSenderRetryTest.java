package org.smu.randsome.randsomeback.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;
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

    @MockitoBean
    VerificationCodeManager verificationCodeManager;

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

    @Test
    void 최종_전송_실패_시_인증_코드가_삭제된다() {
        // given
        String email = "test@sangmyung.kr";
        doThrow(new MailSendException("SMTP 오류"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThatThrownBy(() -> emailSender.send(email, "제목", "본문"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.EMAIL_SEND_FAILED);

        // 3번 재시도 모두 실패 후 recover 호출 → 인증 코드 무효화 1번 호출
        verify(verificationCodeManager, times(1)).invalidateVerificationCode(email);
    }

    @Test
    void 전송_성공_시_인증_코드는_삭제되지_않는다() {
        // given
        String email = "test@sangmyung.kr";
        // javaMailSender.send() 정상 동작 (아무것도 throw 하지 않음)

        // when
        emailSender.send(email, "제목", "본문");

        // then: recover가 호출되지 않으므로 코드 무효화도 호출되지 않아야 함
        verify(verificationCodeManager, times(0)).invalidateVerificationCode(email);
    }

}