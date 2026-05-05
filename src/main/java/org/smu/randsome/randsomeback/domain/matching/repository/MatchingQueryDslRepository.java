package org.smu.randsome.randsomeback.domain.matching.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;

public interface MatchingQueryDslRepository {

    List<MatchingApplication> findAllByFilter(MatchingSearchCondition condition, long offset, long limit);

    long countByFilter(MatchingSearchCondition condition);

}