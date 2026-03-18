package org.smu.randsome.randsomeback.domain.candidate.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class CandidateReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateReader candidateReader;

    @Mock
    CandidateJpaRepository candidateJpaRepository;

    @Test
    void 후보자_신청_이력이_없으면_빈_Optional을_반환한다() {
        // given
        Long memberId = 1L;
        given(candidateJpaRepository.findLatestRegistrationStatusByMemberId(memberId, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when
        Optional<RegistrationStatus> result = candidateReader.findLatestRegistrationStatus(memberId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 후보자_신청_이력이_있으면_최신_상태를_반환한다() {
        // given
        Long memberId = 1L;
        given(candidateJpaRepository.findLatestRegistrationStatusByMemberId(memberId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(RegistrationStatus.PENDING));

        // when
        Optional<RegistrationStatus> result = candidateReader.findLatestRegistrationStatus(memberId);

        // then
        assertThat(result).contains(RegistrationStatus.PENDING);
    }

}