package org.smu.randsome.randsomeback.domain.candidate.implement;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class CandidateValidatorUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateValidator candidateValidator;

    @Mock
    CandidateJpaRepository candidateJpaRepository;

    @Test
    void 이미_등록된_후보자가_없으면_통과한다() {
        // given
        var memberId = 1L;
        given(candidateJpaRepository.existsByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.APPROVED,
                EntityStatus.ACTIVE
        )).willReturn(false);

        // when & then
        assertThatNoException().isThrownBy(() -> candidateValidator.validateApply(memberId));
    }

    @Test
    void 이미_등록된_후보자가_있으면_예외가_발생한다() {
        // given
        Long memberId = 1L;
        given(candidateJpaRepository.existsByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.APPROVED,
                EntityStatus.ACTIVE
        )).willReturn(true);

        // when & then
        assertThatThrownBy(() -> candidateValidator.validateApply(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_CANDIDATE.getMessage());
    }

}