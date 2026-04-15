package org.smu.randsome.randsomeback.domain.candidate.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.response.Cursor;

class CandidateReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateReader candidateReader;

    @Mock
    CandidateRepository candidateRepository;

    @Test
    void 후보자_신청_이력이_없으면_빈_Optional을_반환한다() {
        // given
        Long memberId = 1L;
        given(candidateRepository.findLatestRegistrationStatusByMemberId(memberId, EntityStatus.ACTIVE))
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
        given(candidateRepository.findLatestRegistrationStatusByMemberId(memberId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(RegistrationStatus.PENDING));

        // when
        Optional<RegistrationStatus> result = candidateReader.findLatestRegistrationStatus(memberId);

        // then
        assertThat(result).contains(RegistrationStatus.PENDING);
    }

    @Test
    void 결과가_size_이하이면_hasNext는_false이고_nextCursor는_null이다() {
        // given
        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);
        var cursor = Cursor.of(null, 10);

        var reg1 = mock(CandidateRegistration.class);
        var reg2 = mock(CandidateRegistration.class);
        given(candidateRepository.findAllByFilter(condition, null, 10)).willReturn(List.of(reg1, reg2));

        // when
        var result = candidateReader.findAllByFilter(condition, cursor);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void 결과가_size_초과이면_hasNext는_true이고_items는_size개이다() {
        // given
        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);
        var cursor = Cursor.of(null, 10);

        List<CandidateRegistration> registrations = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            var reg = mock(CandidateRegistration.class);
            registrations.add(reg);
        }
        given(candidateRepository.findAllByFilter(condition, null, 10)).willReturn(registrations);

        // when
        var result = candidateReader.findAllByFilter(condition, cursor);

        // then
        assertThat(result.items()).hasSize(10);
        assertThat(result.hasNext()).isTrue();
    }

}