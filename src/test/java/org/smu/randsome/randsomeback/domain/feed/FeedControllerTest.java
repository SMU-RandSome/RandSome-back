package org.smu.randsome.randsomeback.domain.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.springframework.http.HttpStatus;

class FeedControllerTest extends ControllerTestSupport {

    @Test
    void lastId_없이_피드_조회에_성공하면_200을_반환한다() {
        // given
        given(feedService.getLatestFeed(null)).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/feed"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull());
    }

    @Test
    void lastId로_신규_피드_폴링에_성공하면_200을_반환한다() {
        // given
        given(feedService.getLatestFeed(any())).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/feed?lastId=5"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull());
    }

}