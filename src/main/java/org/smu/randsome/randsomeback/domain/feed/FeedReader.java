package org.smu.randsome.randsomeback.domain.feed;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class FeedReader {

    private final MatchingFeedEventRepository matchingFeedEventRepository;

    @Transactional(readOnly = true)
    public List<MatchingFeedEvent> getLatest() {
        return matchingFeedEventRepository.findTop10ByStatusOrderByIdDesc(EntityStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<MatchingFeedEvent> getAfter(Long lastId) {
        return matchingFeedEventRepository.findByIdGreaterThanAndStatusOrderByIdDesc(lastId, EntityStatus.ACTIVE);
    }

}