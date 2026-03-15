package org.smu.randsome.randsomeback.domain.matching.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
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
    private final MatchingReader matchingReader;
    private final PaymentManager paymentManager;

    /**
     * 매칭 신청을 생성하고 결제 등록까지 수행한다.
     *
     * @param newMatching 매칭 신청 커맨드
     * @param memberId 신청자 식별자
     */
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

    @Transactional(readOnly = true)
    public List<MatchingApplication> getMyApplications(Long memberId, ApplicationStatus status) {
        return matchingReader.findByMemberAndStatus(memberId, status);
    }

}