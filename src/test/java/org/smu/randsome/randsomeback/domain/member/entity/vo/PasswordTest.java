package org.smu.randsome.randsomeback.domain.member.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordTest extends UnitTestSupport {

    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = MemberFixture.ENCODER;
    }

    @Test
    void 평문_비밀번호로_Password_VO를_생성하면_해시화된_값이_저장된다() {
        // given
        Password password = MemberFixture.password();

        // then
        assertThat(password.hashedValue()).isNotNull();
        assertThat(password.hashedValue()).isNotEqualTo(MemberFixture.DEFAULT_RAW_PASSWORD);
    }

    @Test
    void 올바른_평문_비밀번호는_matches가_true를_반환한다() {
        // given
        Password password = MemberFixture.password();

        // then
        assertThat(password.matches(MemberFixture.DEFAULT_RAW_PASSWORD, encoder)).isTrue();
    }

    @Test
    void 틀린_평문_비밀번호는_matches가_false를_반환한다() {
        // given
        Password password = MemberFixture.password();
        // then
        assertThat(password.matches("wrongPassword!", encoder)).isFalse();
    }

    @Test
    void 동일한_평문으로_생성한_두_Password는_matches가_각각_true를_반환한다() {
        // given
        Password p1 = MemberFixture.password();
        Password p2 = MemberFixture.password();

        // then
        assertThat(p1.matches(MemberFixture.DEFAULT_RAW_PASSWORD, encoder)).isTrue();
        assertThat(p2.matches(MemberFixture.DEFAULT_RAW_PASSWORD, encoder)).isTrue();
    }

}