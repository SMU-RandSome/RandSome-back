package org.smu.randsome.randsomeback.domain.feed;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;

class FeedManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    FeedManager feedManager;

    @Mock
    MatchingFeedEventRepository matchingFeedEventRepository;

    @Test
    void recordCandidateRegistration_호출_시_후보자_등록_피드를_저장한다() {
        // given
        var nickname = "여성#XYZ98765";

        // when
        feedManager.recordCandidateRegistration(nickname);

        // then
        verify(matchingFeedEventRepository).save(any(MatchingFeedEvent.class));
    }

    @Test
    void recordMatchRequest_호출_시_매칭_요청_피드를_저장한다() {
        // given
        var nickname = "남성#ABC12345";
        var requestCount = 3;

        // when
        feedManager.recordMatchRequest(nickname, requestCount);

        // then
        verify(matchingFeedEventRepository).save(any(MatchingFeedEvent.class));
    }

}