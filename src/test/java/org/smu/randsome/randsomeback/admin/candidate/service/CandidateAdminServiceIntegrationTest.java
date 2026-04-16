package org.smu.randsome.randsomeback.admin.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.feed.MatchingFeedEvent;
import org.smu.randsome.randsomeback.domain.feed.MatchingFeedEventRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
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
    void 후보자_승인_시_알림_내역과_피드_이벤트가_저장된다() throws InterruptedException {
        // given
        Member member = MemberFixture.create();
        memberJpaRepository.save(member);

        MemberDevice device = MemberDevice.register(member, "test-fcm-token", LocalDateTime.now());
        memberDeviceJpaRepository.save(device);

        CandidateRegistration registration = CandidateRegistration.apply(member);
        candidateJpaRepository.save(registration);
        Long registrationId = registration.getId();

        // when
        candidateAdminService.approve(registrationId);

        // 비동기 작업 완료 대기
        Thread.sleep(500);

        // then - 피드 (동기, REQUIRES_NEW로 즉시 저장)
        List<MatchingFeedEvent> events = matchingFeedEventRepository.findAll();
        assertThat(events).hasSize(1)
                .extracting(MatchingFeedEvent::getNickname)
                .contains(member.getNickname());

        // then - 알림 (비동기, 대기 후 확인)
        List<Notification> notifications = notificationJpaRepository.findAllByMemberIdAndStatus(
                member.getId(),
                EntityStatus.ACTIVE
        );

        assertThat(notifications).hasSize(1)
                .extracting(Notification::getMemberId)
                .contains(member.getId());

        var candidate = memberJpaRepository.findByIdAndStatus(member.getId(), EntityStatus.ACTIVE).orElseThrow();
        assertThat(candidate.getRole()).isEqualTo(Role.ROLE_CANDIDATE);
    }

}