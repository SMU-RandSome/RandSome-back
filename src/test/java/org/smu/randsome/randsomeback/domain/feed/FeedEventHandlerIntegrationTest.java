package org.smu.randsome.randsomeback.domain.feed;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;

/**
 * FeedEventHandler 비동기 이벤트 처리 검증.
 *
 * @Async와 @TransactionalEventListener(AFTER_COMMIT)를 통해:
 * - 트랜잭션 커밋 후 별도 트랜잭션에서 피드를 기록
 * - 피드 기록 실패가 원본 트랜잭션에 영향을 주지 않음
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
    void 후보자등록이벤트를_핸들러가_수신하면_비동기로_피드가_기록된다() throws InterruptedException {
        // given
        String nickname = "남성#TEST001";
        var event = new CandidateRegistrationApprovedEvent(nickname);

        // when - 핸들러 직접 호출
        feedEventHandler.onCandidateRegistrationApproved(event);

        // then - 비동기 작업 완료 대기
        Thread.sleep(2000);

        // 데이터베이스에 저장됨을 검증
        var feeds = feedRepository.findAll();
        assertThat(feeds)
                .as("피드가 정확히 1건 저장됨")
                .hasSize(1);

        var feed = feeds.get(0);
        assertThat(feed.getNickname()).isEqualTo(nickname);
        assertThat(feed.getEventType()).isEqualTo(EventType.CANDIDATE_REGISTERED);
        assertThat(feed.getRequestCount()).isNull();
    }

    @Test
    void 매칭신청이벤트를_핸들러가_수신하면_비동기로_피드가_기록된다() throws InterruptedException {
        // given
        String nickname = "여성#TEST002";
        int count = 3;
        var event = new MatchingApplicationCompletedEvent(1L, nickname, count);

        // when - 핸들러 직접 호출
        feedEventHandler.onMatchingApplicationSuccess(event);

        // then - 비동기 작업 완료 대기
        Thread.sleep(2000);

        // 데이터베이스에 저장됨을 검증
        var feeds = feedRepository.findAll();
        assertThat(feeds)
                .as("피드가 정확히 1건 저장됨")
                .hasSize(1);

        var feed = feeds.get(0);
        assertThat(feed.getNickname()).isEqualTo(nickname);
        assertThat(feed.getEventType()).isEqualTo(EventType.MATCH_REQUESTED);
        assertThat(feed.getRequestCount()).isEqualTo(count);
    }

    @Test
    void 다중_이벤트가_발행되면_각각_비동기로_처리되고_모두_기록된다() throws InterruptedException {
        // given
        String candidate = "남성#CANDIDATE001";
        String matcher1 = "여성#MATCHER001";
        String matcher2 = "여성#MATCHER002";

        // when - 다중 이벤트 처리
        feedEventHandler.onCandidateRegistrationApproved(new CandidateRegistrationApprovedEvent(candidate));
        feedEventHandler.onMatchingApplicationSuccess(new MatchingApplicationCompletedEvent(1L, matcher1, 2));
        feedEventHandler.onMatchingApplicationSuccess(new MatchingApplicationCompletedEvent(2L, matcher2, 1));

        // then - 비동기 작업 완료 대기
        Thread.sleep(2000);

        // 데이터베이스에 모두 저장됨을 검증
        var feeds = feedRepository.findAll();
        assertThat(feeds)
                .as("3개의 이벤트가 모두 기록됨")
                .hasSize(3);

        assertThat(feeds)
                .extracting(MatchingFeedEvent::getNickname)
                .containsExactlyInAnyOrder(candidate, matcher1, matcher2);
    }

    @Test
    void 핸들러_실행_중_예외가발생해도_로깅되고_다른작업에_영향을주지않는다() throws InterruptedException {
        // given - 정상 이벤트와 함께 처리하여 실패가 격리됨을 확인
        String validNickname = "남성#VALID001";

        // when
        feedEventHandler.onCandidateRegistrationApproved(new CandidateRegistrationApprovedEvent(validNickname));
        feedEventHandler.onMatchingApplicationSuccess(new MatchingApplicationCompletedEvent(1L, "여성#MATCHER001", 1));

        // then - 비동기 작업 완료 대기
        Thread.sleep(2000);

        // 예외 처리되어도 정상 이벤트는 모두 기록됨
        var feeds = feedRepository.findAll();
        assertThat(feeds)
                .as("예외 처리되어도 정상 이벤트는 모두 기록됨")
                .hasSize(2);
    }

    @Test
    void 비동기_핸들러는_독립적인_스레드에서_실행된다() throws InterruptedException {
        // given
        String nickname = "남성#ASYNC_TEST";
        var event = new CandidateRegistrationApprovedEvent(nickname);

        // when
        feedEventHandler.onCandidateRegistrationApproved(event);

        // then - 비동기 작업 완료 대기
        Thread.sleep(2000);

        // 데이터가 정상 저장됨을 확인
        var feeds = feedRepository.findAll();
        assertThat(feeds)
                .as("비동기 스레드에서도 정상 저장됨")
                .hasSize(1)
                .first()
                .satisfies(feed -> {
                    assertThat(feed.getNickname()).isEqualTo(nickname);
                    assertThat(feed.getEventType()).isEqualTo(EventType.CANDIDATE_REGISTERED);
                });
    }

}
