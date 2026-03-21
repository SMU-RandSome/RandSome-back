package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManagerTest.MutableClock;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class VerificationCodeValidatorTest extends UnitTestSupport {

    private MutableClock clock;
    private VerificationCodeManager manager;
    private VerificationCodeValidator validator;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        VerificationCodeStore store = new VerificationCodeStore(clock);
        manager = new VerificationCodeManager(clock, store);
        validator = new VerificationCodeValidator(clock, store);
    }

    @Test
    void 올바른_인증_코드를_입력하면_검증에_성공한다() {
        var email = "test@sangmyung.kr";
        var code = manager.generateVerificationCode(email);

        assertThatCode(() -> validator.verifyCode(email, code))
                .doesNotThrowAnyException();
    }

    @Test
    void 틀린_인증_코드를_입력하면_예외가_발생한다() {
        var email = "test@sangmyung.kr";
        manager.generateVerificationCode(email);

        assertThatThrownBy(() -> validator.verifyCode(email, "000000"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_MISMATCH);
    }

    @Test
    void 인증_코드_요청_없이_검증하면_예외가_발생한다() {
        assertThatThrownBy(() -> validator.verifyCode("test@sangmyung.kr", "123456"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    void 만료된_인증_코드로_검증하면_예외가_발생한다() {
        var email = "test@sangmyung.kr";
        var code = manager.generateVerificationCode(email);

        clock.advance(Duration.ofMinutes(6));

        assertThatThrownBy(() -> validator.verifyCode(email, code))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_EXPIRED);
    }

    @Test
    void 검증_성공_후_동일한_코드로_재검증하면_예외가_발생한다() {
        var email = "test@sangmyung.kr";
        var code = manager.generateVerificationCode(email);
        validator.verifyCode(email, code);

        assertThatThrownBy(() -> validator.verifyCode(email, code))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_NOT_FOUND);
    }

}