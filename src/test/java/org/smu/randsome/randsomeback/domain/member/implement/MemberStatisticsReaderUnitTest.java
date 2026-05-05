package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class MemberStatisticsReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberStatisticsReader memberStatisticsReader;

    @Mock
    MemberJpaRepository memberJpaRepository;

    @Test
    void 역할에_해당하는_성별_카운트를_조회한다() {
        // given
        var expected = List.of(
                new CandidateGenderCountItem(Gender.MALE, 3),
                new CandidateGenderCountItem(Gender.FEMALE, 2)
        );
        given(memberJpaRepository.findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE))
                .willReturn(expected);

        // when
        var result = memberStatisticsReader.findGenderCountByRole(Role.ROLE_CANDIDATE);

        // then
        assertThat(result).isEqualTo(expected);
        verify(memberJpaRepository).findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);
    }

    @Test
    void 해당_역할의_회원이_없으면_빈_리스트를_반환한다() {
        // given
        given(memberJpaRepository.findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE))
                .willReturn(List.of());

        // when
        var result = memberStatisticsReader.findGenderCountByRole(Role.ROLE_CANDIDATE);

        // then
        assertThat(result).isEmpty();
    }

}
