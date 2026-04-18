package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;

class VerificationCodeManagerTest extends UnitTestSupport {

    @Mock
    VerificationCodeStore codeStore;

    @InjectMocks
    VerificationCodeManager verificationCodeManager;

    @Test
    void 인증_코드는_6자리_영숫자다() {
        var code = verificationCodeManager.generateVerificationCode("test@sangmyung.kr");

        assertThat(code).hasSize(6);
        assertThat(code).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }

    @Test
    void 동일한_이메일에_인증_코드를_재요청하면_새로운_코드가_발급된다() {
        var email = "test@sangmyung.kr";
        var firstCode = verificationCodeManager.generateVerificationCode(email);
        var secondCode = verificationCodeManager.generateVerificationCode(email);

        assertThat(secondCode).hasSize(6);
        assertThat(secondCode).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
        assertThat(firstCode).isNotEqualTo(secondCode);
    }

}
