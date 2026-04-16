package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingSortType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;

class MatchingReaderFindAllByFilterUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingReader matchingReader;

    @Mock
    MatchingRepository matchingRepository;

    @Mock
    MatchingResultJpaRepository matchingResultJpaRepository;

    @Test
    void 데이터_조회와_count_쿼리를_모두_호출한다() {
        var condition = condition();
        var offsetLimit = new OffsetLimit(1, 10);
        var app = mock(MatchingApplication.class);
        given(matchingRepository.findAllByFilter(condition, 0L, 10L)).willReturn(List.of(app));
        given(matchingRepository.countByFilter(condition)).willReturn(1L);

        matchingReader.findAllByFilter(condition, offsetLimit);

        verify(matchingRepository).findAllByFilter(condition, 0L, 10L);
        verify(matchingRepository).countByFilter(condition);
    }

    @Test
    void 조회된_항목이_PageResponse에_담긴다() {
        var condition = condition();
        var offsetLimit = new OffsetLimit(1, 10);
        var app1 = mock(MatchingApplication.class);
        var app2 = mock(MatchingApplication.class);
        given(matchingRepository.findAllByFilter(condition, 0L, 10L)).willReturn(List.of(app1, app2));
        given(matchingRepository.countByFilter(condition)).willReturn(2L);

        PageResponse<MatchingApplication> result = matchingReader.findAllByFilter(condition, offsetLimit);

        assertThat(result.content()).containsExactly(app1, app2);
        assertThat(result.totalElements()).isEqualTo(2L);
    }

    @Test
    void 다음_페이지가_없으면_hasNext가_false다() {
        var condition = condition();
        var offsetLimit = new OffsetLimit(1, 10);
        given(matchingRepository.findAllByFilter(condition, 0L, 10L)).willReturn(List.of(mock(MatchingApplication.class)));
        given(matchingRepository.countByFilter(condition)).willReturn(5L);

        PageResponse<MatchingApplication> result = matchingReader.findAllByFilter(condition, offsetLimit);

        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 다음_페이지가_있으면_hasNext가_true다() {
        var condition = condition();
        var offsetLimit = new OffsetLimit(1, 2);
        given(matchingRepository.findAllByFilter(condition, 0L, 2L)).willReturn(List.of(mock(MatchingApplication.class), mock(MatchingApplication.class)));
        given(matchingRepository.countByFilter(condition)).willReturn(5L);

        PageResponse<MatchingApplication> result = matchingReader.findAllByFilter(condition, offsetLimit);

        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void 결과가_없으면_빈_content와_zero_total을_반환한다() {
        var condition = condition();
        var offsetLimit = new OffsetLimit(1, 10);
        given(matchingRepository.findAllByFilter(condition, 0L, 10L)).willReturn(List.of());
        given(matchingRepository.countByFilter(condition)).willReturn(0L);

        PageResponse<MatchingApplication> result = matchingReader.findAllByFilter(condition, offsetLimit);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.hasNext()).isFalse();
    }

    private MatchingSearchCondition condition() {
        return new MatchingSearchCondition(null, null, null, MatchingSortType.LATEST);
    }

}
