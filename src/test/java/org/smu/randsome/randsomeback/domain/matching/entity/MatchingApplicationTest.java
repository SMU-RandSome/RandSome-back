package org.smu.randsome.randsomeback.domain.matching.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
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
                MatchingApplication::getTotalPrice,
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getApprovedAt,
                MatchingApplication::getRejectedAt,
                MatchingApplication::getCancelledAt
        ).containsExactly(
                member,
                matchingType,
                applicationCount,
                BigDecimal.valueOf(3000),
                ApplicationStatus.PENDING,
                null,
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
    void 매칭_신청을_승인한다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        var now = TestDateTimeUtils.now();

        // when
        application.approve(now);

        // then
        assertThat(application).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getApprovedAt,
                MatchingApplication::getRejectedReason,
                MatchingApplication::getRejectedAt
        ).containsExactly(
                ApplicationStatus.APPROVED,
                now,
                null,
                null
        );
    }

    @Test
    void 매칭_신청을_거절한다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.IDEAL, 1);
        var reason = "조건 미달";
        var now = TestDateTimeUtils.now();

        // when
        application.reject(now, reason);

        // then
        assertThat(application).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getRejectedAt,
                MatchingApplication::getRejectedReason,
                MatchingApplication::getApprovedAt
        ).containsExactly(
                ApplicationStatus.REJECTED,
                now,
                reason,
                null
        );
    }

    @Test
    void 이미_승인된_매칭을_거절할_수_없다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        application.approve(TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> application.reject(TestDateTimeUtils.now(), "사유"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING.getMessage());
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
    void 이미_승인된_매칭을_취소할_수_없다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        application.approve(TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> application.cancel(TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_CANCEL_APPROVED.getMessage());
    }

    @Test
    void 이미_거절된_매칭을_취소할_수_없다() {
        // given
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        application.reject(TestDateTimeUtils.now(), "사유");

        // when & then
        assertThatThrownBy(() -> application.cancel(TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_CANCEL_REJECTED.getMessage());
    }

}