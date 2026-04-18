package org.smu.randsome.randsomeback.domain.feed;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class FeedReaderIntegrationTest extends IntegrationTestSupport {

    final FeedReader feedReader;
    final MatchingFeedEventRepository matchingFeedEventRepository;
    final EntityManager entityManager;

    @Test
    void 최신_피드를_최대_10건_최신순으로_반환한다() {
        // given
        for (int i = 0; i < 12; i++) {
            matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest("남성#TEST000" + i, i + 1));
        }

        // when
        List<MatchingFeedEvent> result = feedReader.getLatest();

        // then
        assertThat(result).hasSize(10);
        assertThat(result).isSortedAccordingTo(
                (a, b) -> Long.compare(b.getId(), a.getId())
        );
    }

    @Test
    void lastId_이후의_이벤트만_최신순으로_반환한다() {
        // given
        var event1 = matchingFeedEventRepository.save(MatchingFeedEvent.recordCandidateRegister("여성#AAAAAA01"));
        var event2 = matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest("남성#BBBBBB02", 2));
        var event3 = matchingFeedEventRepository.save(MatchingFeedEvent.recordCandidateRegister("여성#CCCCCC03"));

        // when
        List<MatchingFeedEvent> result = feedReader.getAfter(event1.getId());

        // then
        assertThat(result).hasSize(2)
                .extracting(MatchingFeedEvent::getId)
                .containsExactly(event3.getId(), event2.getId());
    }

    @Test
    void lastId_이후_이벤트가_없으면_빈_목록을_반환한다() {
        // given
        var event = matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest("남성#DDDDDD04", 1));

        // when
        List<MatchingFeedEvent> result = feedReader.getAfter(event.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 소프트_삭제된_이벤트는_조회되지_않는다() {
        // given
        var active = matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest("남성#EEEEEE05", 1));
        var deleted = matchingFeedEventRepository.save(MatchingFeedEvent.recordCandidateRegister("여성#FFFFFF06"));
        deleted.delete();
        entityManager.flush();

        // when
        List<MatchingFeedEvent> result = feedReader.getLatest();

        // then
        assertThat(result).hasSize(1)
                .extracting(MatchingFeedEvent::getId)
                .containsExactly(active.getId());
    }

}