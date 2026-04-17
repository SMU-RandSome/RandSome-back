package org.smu.randsome.randsomeback.domain.matching.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class MatchingApplicationTest extends UnitTestSupport {

    @Test
    void 매칭_신청을_한다() {
        // given
        var member = mock(Member.class);
        var matchingType = MatchingType.RANDOM;
        var applicationCount = 3;

        // when
        var application = MatchingApplication.apply(member, matchingType, applicationCount);

        // then
        assertThat(application).isNotNull().extracting(
                MatchingApplication::getMember,
                MatchingApplication::getMatchingType,
                MatchingApplication::getApplicationCount,
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getCompletedAt,
                MatchingApplication::getCancelledAt
        ).containsExactly(
                member,
                matchingType,
                applicationCount,
                ApplicationStatus.PENDING,
                null,
                null
        );
    }

    @Test
    void 매칭_신청_인원수가_유효하지_않다() {
        // given
        var member = mock(Member.class);

        // when & then
        assertThatThrownBy(() -> MatchingApplication.apply(member, MatchingType.RANDOM, 0))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PERSON_COUNT.getMessage());

        assertThatThrownBy(() -> MatchingApplication.apply(member, MatchingType.RANDOM, 6))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PERSON_COUNT.getMessage());
    }

    @Test
    void 매칭_신청을_완료한다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 3);
        var now = TestDateTimeUtils.now();

        // when
        application.complete(now, 3);

        // then
        assertThat(application).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getCompletedAt,
                MatchingApplication::getMatchedCount
        ).containsExactly(
                ApplicationStatus.SUCCESS,
                now,
                3
        );
    }

    @Test
    void 부분_매칭_완료_시_matchedCount에_실제_매칭_수가_저장된다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 5);
        var now = TestDateTimeUtils.now();

        // when
        application.complete(now, 3);

        // then
        assertThat(application.getMatchedCount()).isEqualTo(3);
        assertThat(application.getApplicationCount()).isEqualTo(5);
        assertThat(application.getApplicationStatus()).isEqualTo(ApplicationStatus.SUCCESS);
    }

    @Test
    void 후보자_없이_완료_시_matchedCount가_0이다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 3);

        // when
        application.complete(TestDateTimeUtils.now(), 0);

        // then
        assertThat(application.getMatchedCount()).isZero();
        assertThat(application.getApplicationStatus()).isEqualTo(ApplicationStatus.SUCCESS);
    }

    @Test
    void 매칭_신청을_취소한다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        var now = TestDateTimeUtils.now();

        // when
        application.cancel(now);

        // then
        assertThat(application).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getCancelledAt
        ).containsExactly(
                ApplicationStatus.CANCELLED,
                now
        );
    }

    @Test
    void 이미_완료된_매칭을_취소할_수_없다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        application.complete(TestDateTimeUtils.now(), 2);

        // when & then
        assertThatThrownBy(() -> application.cancel(TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_CANCEL_APPROVED.getMessage());
    }

}