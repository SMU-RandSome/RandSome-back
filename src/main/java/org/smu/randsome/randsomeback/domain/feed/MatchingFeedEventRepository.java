package org.smu.randsome.randsomeback.domain.feed;

import java.util.List;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingFeedEventRepository extends JpaRepository<MatchingFeedEvent, Long> {

    List<MatchingFeedEvent> findTop10ByStatusOrderByIdDesc(EntityStatus status);

    List<MatchingFeedEvent> findTop10ByIdGreaterThanAndStatusOrderByIdDesc(Long lastId, EntityStatus status);

}