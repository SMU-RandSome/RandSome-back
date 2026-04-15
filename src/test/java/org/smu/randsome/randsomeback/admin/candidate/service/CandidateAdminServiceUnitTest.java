package org.smu.randsome.randsomeback.admin.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;

class CandidateAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateAdminService candidateAdminService;

    @Mock
    CandidateManager candidateManager;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void 후보자_승인시_이벤트가_발행된다() {
        // given
        Long registrationId = 1L;
        String nickname = "테스트유저";

        var member = mock(Member.class);
        given(member.getNickname()).willReturn(nickname);

        var registration = mock(CandidateRegistration.class);
        given(registration.getMember()).willReturn(member);

        given(candidateManager.approve(registrationId))
                .willReturn(registration);

        // when
        candidateAdminService.approve(registrationId);

        // then
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        then(eventPublisher).should().publishEvent(captor.capture());

        Object publishedEvent = captor.getValue();
        assertThat(publishedEvent).isInstanceOf(CandidateRegistrationApprovedEvent.class);
        assertThat(((CandidateRegistrationApprovedEvent) publishedEvent).nickname()).isEqualTo(nickname);
    }

    @Test
    void 후보자_승인시_manager의_approve가_호출된다() {
        // given
        Long registrationId = 1L;

        var member = mock(Member.class);
        var registration = mock(CandidateRegistration.class);
        given(registration.getMember()).willReturn(member);
        given(candidateManager.approve(registrationId))
                .willReturn(registration);

        // when
        candidateAdminService.approve(registrationId);

        // then
        then(candidateManager).should().approve(registrationId);
    }

    @Test
    void 후보자_거절시_manager의_reject가_호출된다() {
        // given
        Long registrationId = 1L;
        String rejectionReason = "자격 미달";

        // when
        candidateAdminService.reject(registrationId, rejectionReason);

        // then
        then(candidateManager).should().reject(registrationId, rejectionReason);
    }

}