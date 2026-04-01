package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.strategy.MatchingStrategy;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class MatchingManagerUnitTest extends UnitTestSupport {

    MatchingManager matchingManager;

    @Mock
    MatchingJpaRepository matchingJpaRepository;

    @Mock
    MatchingResultJpaRepository matchingResultJpaRepository;

    @Mock
    MemberReader memberReader;

    @Mock
    MatchingStrategy randomStrategy;

    @Mock
    MatchingStrategy idealStrategy;

    @Mock
    RedisRepository redisRepository;

    @BeforeEach
    void setUp() {
        matchingManager = new MatchingManager(
                matchingJpaRepository,
                matchingResultJpaRepository,
                redisRepository,
                memberReader,
                List.of(randomStrategy, idealStrategy)
        );
    }

    @Test
    void 매칭_신청을_저장하고_반환한다() {
        // given
        var memberId = 1L;
        var member = mock(Member.class);
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        given(memberReader.findWithLock(memberId)).willReturn(member);
        given(redisRepository.tryAcquire(any(String.class), any())).willReturn(true);
        given(matchingJpaRepository.save(any(MatchingApplication.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        MatchingApplication result = matchingManager.apply(newMatching, memberId);

        // then
        assertThat(result).isNotNull().extracting(
                MatchingApplication::getMember,
                MatchingApplication::getMatchingType,
                MatchingApplication::getApplicationCount,
                MatchingApplication::getApplicationStatus
        ).containsExactly(
                member,
                MatchingType.RANDOM,
                3,
                ApplicationStatus.PENDING
        );
    }

    @Test
    void 동일_파라미터로_10초_내_재신청하면_TOO_MANY_MATCHING_REQUESTS를_던진다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();
        var member = mock(Member.class);

        given(memberReader.findWithLock(memberId)).willReturn(member);
        given(redisRepository.tryAcquire(any(String.class), any())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.TOO_MANY_MATCHING_REQUESTS.getMessage());
    }

    @Test
    void 회원이_없으면_예외가_발생한다() {
        // given
        var memberId = 999L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();
        given(memberReader.findWithLock(memberId))
                .willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 매칭_신청을_승인하면_전략을_통해_매칭_결과를_저장한다() {
        // given
        var id = 1L;
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        var approvedAt = TestDateTimeUtils.now();

        given(matchingJpaRepository.findByIdAndStatusWithMember(id, EntityStatus.ACTIVE))
                .willReturn(Optional.of(application));
        given(randomStrategy.getSupportedType()).willReturn(MatchingType.RANDOM);
        given(randomStrategy.execute(application)).willReturn(List.of());

        // when
        matchingManager.approve(id, approvedAt);

        // then
        assertThat(application).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getApprovedAt,
                MatchingApplication::getRejectedAt,
                MatchingApplication::getRejectedReason
        ).containsExactly(
                ApplicationStatus.APPROVED,
                approvedAt,
                null,
                null
        );
    }

    @Test
    void 승인할_매칭이_없으면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var id = 999L;
        given(matchingJpaRepository.findByIdAndStatusWithMember(id, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchingManager.approve(id, TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void 매칭_신청을_거절한다() {
        // given
        var id = 1L;
        var reason = "서류 미비";
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        var rejectedAt = TestDateTimeUtils.now();
        given(matchingJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE))
                .willReturn(Optional.of(application));

        // when
        matchingManager.reject(id, reason, rejectedAt);

        // then
        assertThat(application).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getRejectedReason,
                MatchingApplication::getRejectedAt,
                MatchingApplication::getApprovedAt
        ).containsExactly(
                ApplicationStatus.REJECTED,
                reason,
                rejectedAt,
                null
        );
    }

    @Test
    void 거절할_매칭이_없으면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var id = 999L;
        given(matchingJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchingManager.reject(id, "사유", TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void 매칭_신청을_취소하면_CANCELLED_상태와_취소_시각이_기록된다() {
        // given
        var applicationId = 1L;
        var memberId = 1L;
        var member = mock(Member.class);
        var application = MatchingApplication.apply(member, MatchingType.RANDOM, 2);
        given(matchingJpaRepository.findByIdAndMemberIdAndStatus(applicationId, memberId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(application));

        // when
        MatchingApplication result = matchingManager.cancel(applicationId, memberId);

        // then
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    void 취소할_매칭이_없으면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var applicationId = 999L;
        var memberId = 1L;
        given(matchingJpaRepository.findByIdAndMemberIdAndStatus(applicationId, memberId, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchingManager.cancel(applicationId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

}