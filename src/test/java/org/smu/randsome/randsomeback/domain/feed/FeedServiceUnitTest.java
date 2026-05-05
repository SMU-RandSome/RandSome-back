package org.smu.randsome.randsomeback.domain.feed;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;

class FeedServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    FeedService feedService;

    @Mock
    FeedReader feedReader;

    @Test
    void lastId가_null이면_getLatest를_호출한다() {
        // when
        feedService.getLatestFeed(null);

        // then
        verify(feedReader).getLatest();
    }

    @Test
    void lastId가_있으면_getAfter를_호출한다() {
        // given
        var lastId = 5L;

        // when
        feedService.getLatestFeed(lastId);

        // then
        verify(feedReader).getAfter(lastId);
    }

}
