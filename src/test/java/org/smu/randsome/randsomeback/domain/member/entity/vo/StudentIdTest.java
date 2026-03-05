package org.smu.randsome.randsomeback.domain.member.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class StudentIdTest extends UnitTestSupport {

    @Test
    void 유효한_이메일로_StudentId_VO를_생성한다() {
        // given
        StudentId studentId = MemberFixture.studentId();

        // then
        assertThat(studentId.number()).isEqualTo("202312345");
    }

    @Test
    void null_이메일로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> StudentId.create(null))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_STUDENT_ID_FORMAT.getMessage());
    }

    @Test
    void 이메일_형식이_아닌_값으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> StudentId.create("not-an-email"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_STUDENT_ID_FORMAT.getMessage());
    }

    @Test
    void 학번이_9자리_숫자가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> StudentId.create("2023123@sangmyung.kr"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_STUDENT_ID_FORMAT.getMessage());
    }

    @Test
    void 유효_범위_밖의_연도면_예외가_발생한다() {
        assertThatThrownBy(() -> StudentId.create("201912345@sangmyung.kr"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_STUDENT_ID_YEAR.getMessage());
    }

    @Test
    void 동일한_이메일로_생성한_두_StudentId_VO는_동등하다() {
        // given
        StudentId id1 = MemberFixture.studentId();
        StudentId id2 = MemberFixture.studentId();

        // then
        assertThat(id1).isEqualTo(id2);
    }

    @Test
    void 다른_이메일로_생성한_두_StudentId_VO는_동등하지_않다() {
        // given
        StudentId id1 = StudentId.create("202312345@sangmyung.kr");
        StudentId id2 = StudentId.create("202312346@sangmyung.kr");

        // then
        assertThat(id1).isNotEqualTo(id2);
    }

}