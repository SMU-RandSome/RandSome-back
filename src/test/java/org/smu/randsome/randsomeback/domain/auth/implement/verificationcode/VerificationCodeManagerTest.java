package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class VerificationCodeManagerTest extends UnitTestSupport {

    @Mock
    VerificationCodeStore codeStore;

    @Mock
    RedisRepository redisRepository;

    @InjectMocks
    VerificationCodeManager verificationCodeManager;

    @Test
    void 인증_코드는_6자리_영숫자다() {
        given(redisRepository.tryAcquire(anyString(), any())).willReturn(true);

        var code = verificationCodeManager.generateVerificationCode("test@sangmyung.kr");

        assertThat(code).hasSize(6);
        assertThat(code).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }

    @Test
    void 동일한_이메일에_인증_코드를_재요청하면_새로운_코드가_발급된다() {
        var email = "test@sangmyung.kr";
        given(redisRepository.tryAcquire(anyString(), any())).willReturn(true);

        var firstCode = verificationCodeManager.generateVerificationCode(email);
        var secondCode = verificationCodeManager.generateVerificationCode(email);

        assertThat(secondCode).hasSize(6);
        assertThat(secondCode).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
        assertThat(firstCode).isNotEqualTo(secondCode);
    }

    @Test
    void 쿨다운_기간_내_재요청하면_예외가_발생한다() {
        given(redisRepository.tryAcquire(anyString(), any())).willReturn(false);

        assertThatThrownBy(() -> verificationCodeManager.generateVerificationCode("test@sangmyung.kr"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_SEND_COOLDOWN);
    }

}
