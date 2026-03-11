package org.smu.randsome.randsomeback.domain.candidate.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;

class CandidateManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateManager candidateManager;

    @Mock
    CandidateJpaRepository candidateJpaRepository;

    @Mock
    MemberReader memberReader;

    @Test
    void 후보자_지원을_저장하고_반환한다() {
        // given
        var memberId = 1L;
        var member = mock(Member.class);
        given(memberReader.find(memberId)).willReturn(member);
        given(candidateJpaRepository.save(any(CandidateRegistration.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CandidateRegistration result = candidateManager.apply(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
    }

}