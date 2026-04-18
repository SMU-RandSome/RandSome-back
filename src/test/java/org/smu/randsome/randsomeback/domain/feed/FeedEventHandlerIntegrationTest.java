package org.smu.randsome.randsomeback.domain.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;

/**
 * FeedEventHandler 비동기 이벤트 처리 검증.
 *
 * @Async와 @TransactionalEventListener(AFTER_COMMIT)를 통해: - 트랜잭션 커밋 후 별도 트랜잭션에서 피드를 기록 - 피드 기록 실패가 원본 트랜잭션에 영향을 주지 않음
 */
@RequiredArgsConstructor
class FeedEventHandlerIntegrationTest extends IntegrationTestSupport {

    final FeedEventHandler feedEventHandler;
    final MatchingFeedEventRepository feedRepository;

    @BeforeEach
    void setUp() {
        feedRepository.deleteAll();
    }

    @Test
    void 후보자등록이벤트를_핸들러가_수신하면_비동기로_피드가_기록된다() {
        // given
        String nickname = "남성#TEST001";
        var event = new CandidateRegistrationApprovedEvent(nickname);

        // when - 핸들러 직접 호출
        feedEventHandler.onCandidateRegistrationApproved(event);

        // then - DB에 정확히 1건 저장될 때까지 대기 (최대 5초)
        await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var feeds = feedRepository.findAll();
                    assertThat(feeds)
                            .as("피드가 정확히 1건 저장됨")
                            .hasSize(1);

                    var feed = feeds.getFirst();
                    assertThat(feed.getNickname()).isEqualTo(nickname);
                    assertThat(feed.getEventType()).isEqualTo(EventType.CANDIDATE_REGISTERED);
                    assertThat(feed.getRequestCount()).isNull();
                });
    }

    @Test
    void 매칭신청이벤트를_핸들러가_수신하면_비동기로_피드가_기록된다() {
        // given
        String nickname = "여성#TEST002";
        int count = 3;
        var event = new MatchingApplicationCompletedEvent(1L, nickname, count);

        // when - 핸들러 직접 호출
        feedEventHandler.onMatchingApplicationSuccess(event);

        // then - DB에 정확히 1건 저장될 때까지 대기 (최대 5초)
        await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var feeds = feedRepository.findAll();
                    assertThat(feeds)
                            .as("피드가 정확히 1건 저장됨")
                            .hasSize(1);

                    var feed = feeds.getFirst();
                    assertThat(feed.getNickname()).isEqualTo(nickname);
                    assertThat(feed.getEventType()).isEqualTo(EventType.MATCH_REQUESTED);
                    assertThat(feed.getRequestCount()).isEqualTo(count);
                });
    }

    @Test
    void 다중_이벤트가_발행되면_각각_비동기로_처리되고_모두_기록된다() {
        // given
        String candidate = "남성#CANDIDATE001";
        String matcher1 = "여성#MATCHER001";
        String matcher2 = "여성#MATCHER002";

        // when
        feedEventHandler.onCandidateRegistrationApproved(new CandidateRegistrationApprovedEvent(candidate));
        feedEventHandler.onMatchingApplicationSuccess(new MatchingApplicationCompletedEvent(1L, matcher1, 2));
        feedEventHandler.onMatchingApplicationSuccess(new MatchingApplicationCompletedEvent(2L, matcher2, 1));

        // then
        await()
                .timeout(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var feeds = feedRepository.findAll();

                    assertThat(feeds)
                            .as("3개의 이벤트가 모두 기록됨")
                            .hasSize(3);

                    assertThat(feeds)
                            .extracting(MatchingFeedEvent::getNickname)
                            .containsExactlyInAnyOrder(candidate, matcher1, matcher2);
                });
    }

}
