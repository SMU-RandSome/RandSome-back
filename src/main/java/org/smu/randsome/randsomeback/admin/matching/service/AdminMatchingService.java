package org.smu.randsome.randsomeback.admin.matching.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminMatchingService {

    private final MatchingReader matchingReader;

    public PageResponse<MatchingApplication> findMatchings(MatchingSearchCondition condition, OffsetLimit offsetLimit) {
        return matchingReader.findAllByFilter(condition, offsetLimit);
    }

}