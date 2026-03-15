package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class MatchingReaderUnitTest extends UnitTestSupport {

    MatchingReader matchingReader;

    @Mock
    MatchingJpaRepository matchingJpaRepository;

    @BeforeEach
    void setUp() {
        matchingReader = new MatchingReader(matchingJpaRepository);
    }

    @Test
    void 회원과_신청상태로_목록을_조회한다() {
        // given
        var memberId = 1L;
        var status = ApplicationStatus.PENDING;
        var expected = List.of(mock(MatchingApplication.class));
        given(matchingJpaRepository.findAllByMemberIdAndApplicationStatusAndStatus(
                memberId,
                status,
                EntityStatus.ACTIVE
        )).willReturn(expected);

        // when
        var result = matchingReader.findByMemberAndStatus(memberId, status);

        // then
        assertThat(result).isEqualTo(expected);
        verify(matchingJpaRepository).findAllByMemberIdAndApplicationStatusAndStatus(
                memberId,
                status,
                EntityStatus.ACTIVE
        );
    }

}
