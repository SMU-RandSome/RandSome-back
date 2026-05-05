package org.smu.randsome.randsomeback.domain.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class FeedManager {

    private final MatchingFeedEventRepository matchingFeedEventRepository;

    @Transactional
    public void recordCandidateRegistration(String nickname) {
        matchingFeedEventRepository.save(MatchingFeedEvent.recordCandidateRegister(nickname));

        log.info("[FeedManager] 후보자 등록 피드 기록 완료 - nickname={}", nickname);
    }

    @Transactional
    public void recordMatchRequest(String nickname, Integer requestCount) {
        matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest(
                nickname,
                requestCount
        ));

        log.info("[FeedManager] 매칭 요청 피드 기록 완료 - nickname={}, requestCount={}", nickname, requestCount);
    }

}