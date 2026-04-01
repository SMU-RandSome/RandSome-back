package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingManagerIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MemberJpaRepository memberJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;
    final RedisRepository redisRepository;

    @Test
    void 매칭_신청을_저장하면_PENDING_상태로_DB에_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        var result = matchingManager.apply(newMatching, member.getId());

        // then
        var saved = matchingJpaRepository.findById(result.getId()).orElseThrow();
        assertThat(saved).extracting(
                MatchingApplication::getId,
                MatchingApplication::getMember,
                MatchingApplication::getMatchingType,
                MatchingApplication::getApplicationCount,
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getApprovedAt,
                MatchingApplication::getRejectedAt,
                MatchingApplication::getCancelledAt
        ).containsExactly(
                result.getId(),
                member,
                MatchingType.RANDOM,
                3,
                ApplicationStatus.PENDING,
                null,
                null,
                null
        );
    }

    @Test
    void 동일_파라미터로_10초_내_재신청하면_TOO_MANY_MATCHING_REQUESTS를_던진다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();
        matchingManager.apply(newMatching, member.getId());

        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.TOO_MANY_MATCHING_REQUESTS.getMessage());
    }

    @Test
    void 인원수가_다르면_같은_타입으로_재신청해도_정상_처리된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        matchingManager.apply(NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build(), member.getId());

        // when
        var result = matchingManager.apply(NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(5)
                .build(), member.getId());

        // then
        assertThat(result.getApplicationCount()).isEqualTo(5);
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void 존재하지_않는_회원이면_NOT_FOUND_MEMBER를_던진다() {
        // given
        var nonExistentMemberId = 999L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.IDEAL)
                .applicationCount(2)
                .build();

        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, nonExistentMemberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 매칭_신청을_승인하면_APPROVED_상태와_승인_시각이_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();
        var application = matchingManager.apply(newMatching, member.getId());
        var approvedAt = TestDateTimeUtils.now();

        // when
        matchingManager.approve(application.getId(), approvedAt);

        // then
        var result = matchingJpaRepository.findById(application.getId()).orElseThrow();
        assertThat(result).extracting(
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
    void 존재하지_않는_매칭을_승인하면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> matchingManager.approve(nonExistentId, TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void 매칭_신청을_거절하면_REJECTED_상태와_거절_사유_및_거절_시각이_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.IDEAL)
                .applicationCount(2)
                .build();
        var application = matchingManager.apply(newMatching, member.getId());
        var reason = "서류 미비";
        var rejectedAt = TestDateTimeUtils.now();

        // when
        matchingManager.reject(application.getId(), reason, rejectedAt);

        // then
        MatchingApplication result = matchingJpaRepository.findById(application.getId()).orElseThrow();

        assertThat(result).extracting(
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
    void 존재하지_않는_매칭을_거절하면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> matchingManager.reject(nonExistentId, "사유", TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void PENDING_신청을_취소하면_CANCELLED_상태와_취소_시각이_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();
        var application = matchingManager.apply(newMatching, member.getId());

        // when
        matchingManager.cancel(application.getId(), member.getId());

        // then
        var result = matchingJpaRepository.findById(application.getId()).orElseThrow();
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_신청을_취소하면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> matchingManager.cancel(nonExistentId, member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void 다른_사용자의_신청을_취소하면_NOT_FOUND_MATCHING을_던진다() {
        // given
        var owner = memberJpaRepository.save(MemberFixture.create());
        var other = memberJpaRepository.save(MemberFixture.createWithGender("202300000@sangmyung.kr", owner.getGender()));
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(1)
                .build();
        var application = matchingManager.apply(newMatching, owner.getId());

        // when & then
        assertThatThrownBy(() -> matchingManager.cancel(application.getId(), other.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void APPROVED_신청을_취소하면_NOT_ALLOW_CANCEL_APPROVED를_던진다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();
        var application = matchingManager.apply(newMatching, member.getId());
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> matchingManager.cancel(application.getId(), member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_CANCEL_APPROVED.getMessage());
    }

    @Test
    void REJECTED_신청을_취소하면_NOT_ALLOW_CANCEL_REJECTED를_던진다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();
        var application = matchingManager.apply(newMatching, member.getId());
        matchingManager.reject(application.getId(), "사유", TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> matchingManager.cancel(application.getId(), member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_CANCEL_REJECTED.getMessage());
    }

}