package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;

class VerificationCodeManagerTest extends UnitTestSupport {

    private VerificationCodeManager verificationCodeManager;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.system(ZoneId.of("Asia/Seoul"));
        verificationCodeManager = new VerificationCodeManager(clock, new VerificationCodeStore(clock));
    }

    @Test
    void 인증_코드는_6자리_영숫자다() {
        var code = verificationCodeManager.generateVerificationCode("test@sangmyung.kr");

        assertThat(code).hasSize(6);
        assertThat(code).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }

    @Test
    void 동일한_이메일에_인증_코드를_재요청하면_새로운_코드가_발급된다() {
        var email = "test@sangmyung.kr";
        verificationCodeManager.generateVerificationCode(email);

        var newCode = verificationCodeManager.generateVerificationCode(email);

        assertThat(newCode).hasSize(6);
        assertThat(newCode).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }

    @Test
    void 서로_다른_이메일은_독립적인_인증_코드를_가진다() {
        var codeA = verificationCodeManager.generateVerificationCode("student1@sangmyung.kr");
        var codeB = verificationCodeManager.generateVerificationCode("student2@sangmyung.kr");

        assertThat(codeA).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
        assertThat(codeB).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
        assertThat(codeA).isNotEqualTo(codeB);
    }

    // -----------------------------------------------------------------------
    // 테스트용 가변 Clock
    // -----------------------------------------------------------------------

    static class MutableClock extends Clock {

        private Instant now = Instant.now();
        private final ZoneId zone = ZoneId.of("Asia/Seoul");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override public ZoneId getZone() { return zone; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

}