package org.smu.randsome.randsomeback.admin.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.feed.MatchingFeedEvent;
import org.smu.randsome.randsomeback.domain.feed.MatchingFeedEventRepository;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberDeviceJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.notification.entity.Notification;
import org.smu.randsome.randsomeback.domain.notification.repository.NotificationJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

@RequiredArgsConstructor
class CandidateAdminServiceIntegrationTest extends IntegrationTestSupport {

    final CandidateJpaRepository candidateJpaRepository;
    final MemberJpaRepository memberJpaRepository;
    final MatchingFeedEventRepository matchingFeedEventRepository;
    final CandidateAdminService candidateAdminService;
    final NotificationJpaRepository notificationJpaRepository;
    final MemberDeviceJpaRepository memberDeviceJpaRepository;

    @AfterEach
    void tearDown() {
        notificationJpaRepository.deleteAll();
        memberDeviceJpaRepository.deleteAll();
        matchingFeedEventRepository.deleteAll();
        candidateJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 후보자_승인_시_권한_업데이트와_알림_내역과_피드_이벤트가_저장된다() {
        // given
        var member = MemberFixture.create();
        memberJpaRepository.save(member);

        var device = MemberDevice.register(member, "test-fcm-token", LocalDateTime.now());
        memberDeviceJpaRepository.save(device);

        var registration = CandidateRegistration.apply(member);
        candidateJpaRepository.save(registration);
        var registrationId = registration.getId();

        // when
        candidateAdminService.approve(registrationId);

        // then - 피드 (동기, REQUIRES_NEW로 즉시 저장)
        assertThat(matchingFeedEventRepository.findAll())
                .hasSize(1)
                .extracting(MatchingFeedEvent::getNickname)
                .contains(member.getNickname());

        // then - 알림 (비동기, Awaitility로 조건 충족까지 대기)
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<Notification> notifications = notificationJpaRepository.findAllByMemberIdAndStatus(
                            member.getId(),
                            EntityStatus.ACTIVE
                    );
                    assertThat(notifications).hasSize(1)
                            .extracting(Notification::getMemberId)
                            .contains(member.getId());
                });

        // then - 권한 업데이트 확인
        var updatedMember = memberJpaRepository.findByIdAndStatus(member.getId(), EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(updatedMember.getRole()).isEqualTo(Role.ROLE_CANDIDATE);
    }

    @Test
    void 후보자_거절_시_후보자_거절_사유와_알림_내역이_저장된다() {
        // given
        var member = MemberFixture.create();
        memberJpaRepository.save(member);

        var device = MemberDevice.register(member, "test-fcm-token", LocalDateTime.now());
        memberDeviceJpaRepository.save(device);

        var registration = CandidateRegistration.apply(member);
        candidateJpaRepository.save(registration);
        var registrationId = registration.getId();

        // when
        candidateAdminService.reject(registrationId, "부적합한 지원입니다.");

        // then - 후보자 등록 상태 업데이트 확인
        var candidateRegistration = candidateJpaRepository.findByIdAndStatus(registrationId,
                        EntityStatus.ACTIVE)
                .orElseThrow();

        assertThat(candidateRegistration).extracting(
                CandidateRegistration::getRegistrationStatus,
                CandidateRegistration::getRejectedReason
        ).containsExactly(
                RegistrationStatus.REJECTED,
                "부적합한 지원입니다."
        );

        // then - 알림 (비동기, Awaitility로 조건 충족까지 대기)
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<Notification> notifications = notificationJpaRepository.findAllByMemberIdAndStatus(
                            member.getId(),
                            EntityStatus.ACTIVE
                    );
                    assertThat(notifications).hasSize(1)
                            .extracting(Notification::getMemberId)
                            .contains(member.getId());
                });

        // then - 권한 업데이트 확인
        var updatedMember = memberJpaRepository.findByIdAndStatus(member.getId(), EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(updatedMember.getRole()).isEqualTo(Role.ROLE_MEMBER);
    }

}