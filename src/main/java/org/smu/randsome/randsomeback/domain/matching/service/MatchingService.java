package org.smu.randsome.randsomeback.domain.matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.service.command.NewMatching;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingService {

    private final MatchingManager matchingManager;
    private final PaymentManager paymentManager;

    @Transactional
    public void apply(NewMatching newMatching, Long memberId) {
        MatchingApplication matchingApplication = matchingManager.apply(newMatching, memberId);

        paymentManager.register(
                matchingApplication.getMember(),
                PaymentType.from(matchingApplication.getMatchingType()),
                matchingApplication.getId(),
                matchingApplication.getApplicationCount()
        );

        log.info("[MatchingService] 매칭 신청 완료 - memberId: {}, matchingType: {}, applicationCount: {}",
                memberId, matchingApplication.getMatchingType(), matchingApplication.getApplicationCount());
    }

}