package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class VerificationCodeValidatorTest extends UnitTestSupport {

    @Mock
    VerificationCodeStore codeStore;

    @InjectMocks
    VerificationCodeValidator validator;

    @Test
    void 올바른_인증_코드를_입력하면_검증에_성공한다() {
        var email = "test@sangmyung.kr";
        var code = "ABC123";
        given(codeStore.get(email)).willReturn(code);
        given(codeStore.removeIfPresent(email, code)).willReturn(true);

        assertThatCode(() -> validator.verifyCode(email, code))
                .doesNotThrowAnyException();
    }

    @Test
    void 틀린_인증_코드를_입력하면_예외가_발생한다() {
        var email = "test@sangmyung.kr";
        given(codeStore.get(email)).willReturn("ABC123");

        assertThatThrownBy(() -> validator.verifyCode(email, "000000"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_MISMATCH);
    }

    @Test
    void 코드가_존재하지_않으면_예외가_발생한다() {
        // 미발급 또는 Redis TTL 만료 시 GET은 null을 반환한다
        var email = "test@sangmyung.kr";
        given(codeStore.get(email)).willReturn(null);

        assertThatThrownBy(() -> validator.verifyCode(email, "ABC123"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    void 동시에_같은_코드로_검증하면_하나만_성공한다() {
        // get()은 코드를 반환하지만 removeIfPresent()가 false → 다른 요청이 먼저 소비한 상황
        var email = "test@sangmyung.kr";
        var code = "ABC123";
        given(codeStore.get(email)).willReturn(code);
        given(codeStore.removeIfPresent(email, code)).willReturn(false);

        assertThatThrownBy(() -> validator.verifyCode(email, code))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_NOT_FOUND);
    }

}