package org.smu.randsome.randsomeback.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;

@RequiredArgsConstructor
class EmailSenderIntegrationTest extends IntegrationTestSupport {

    final EmailSender emailSender;

    @Disabled
    @Test
    void 이메일에_인증코드가_성공적으로_전송된다() {
        // given
        var email = "202221033@sangmyung.kr";
        var verificationCode = "123456";

        // when
        emailSender.send(email, EMAIL.EMAIL_SUBJECT.getValue(), EMAIL.EMAIL_BODY_TEMPLATE.getValue().formatted(verificationCode));

        // then
        // 실제 이메일이 전송되는지 여부는 수동으로 확인해야 합니다.
        // 이메일 클라이언트에서 "
    }

}