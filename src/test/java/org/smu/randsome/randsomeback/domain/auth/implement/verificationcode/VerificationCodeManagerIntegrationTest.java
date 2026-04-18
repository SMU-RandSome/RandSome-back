package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;

@RequiredArgsConstructor
class VerificationCodeManagerIntegrationTest extends IntegrationTestSupport {

    final VerificationCodeManager verificationCodeManager;
    final VerificationCodeStore codeStore;

    @Test
    void 인증코드_생성_시_캐시에_저장된다() {
        // given
        var email = "202221033@sangmyeng.kr";

        // when
        var code = verificationCodeManager.generateVerificationCode(email);

        // then
        var verificationCode = codeStore.get(email);
        assertThat(verificationCode).isEqualTo(code);
    }

    @Test
    void 인증코드를_캐시에서_삭제한다() {
        // given
        var email = "202221033@sangmyeng.kr";
        var code = verificationCodeManager.generateVerificationCode(email);

        // when
        verificationCodeManager.invalidateVerificationCode(email);
        // then
        var verificationCode = codeStore.get(email);
        assertThat(verificationCode).isNull();
    }

}