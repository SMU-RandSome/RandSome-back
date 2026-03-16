package org.smu.randsome.randsomeback.domain.feed;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FeedService {

    private final FeedReader feedReader;

    public List<MatchingFeedEvent> getLatestFeed(Long lastId) {
        if (lastId == null) {
            return feedReader.getLatest();
        }
        return feedReader.getAfter(lastId);
    }

}
