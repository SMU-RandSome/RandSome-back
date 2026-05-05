package org.smu.randsome.randsomeback.domain.member.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class EmailTest extends UnitTestSupport {

    @Test
    void 유효한_상명대_이메일로_Email_VO를_생성한다() {
        // given
        Email email = MemberFixture.email();

        // then
        assertThat(email.address()).isEqualTo(MemberFixture.DEFAULT_EMAIL);
    }

    @Test
    void 상명대_도메인이_아닌_이메일은_예외가_발생한다() {
        assertThatThrownBy(() -> new Email("test@gmail.com"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_EMAIL_DOMAIN);
    }

    @Test
    void 이메일_형식이_아닌_값은_예외가_발생한다() {
        assertThatThrownBy(() -> new Email("not-an-email"))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    void null_이메일은_예외가_발생한다() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    void 동일한_이메일_값을_가진_두_Email_VO는_동등하다() {
        // given
        Email email1 = MemberFixture.email();
        Email email2 = MemberFixture.email();

        // then
        assertThat(email1).isEqualTo(email2);
    }

    @Test
    void 다른_이메일_값을_가진_두_Email_VO는_동등하지_않다() {
        // given
        Email email1 = new Email("student1@sangmyung.kr");
        Email email2 = new Email("student2@sangmyung.kr");

        // then
        assertThat(email1).isNotEqualTo(email2);
    }

}