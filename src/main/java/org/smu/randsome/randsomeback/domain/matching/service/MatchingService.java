package org.smu.randsome.randsomeback.domain.matching.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingAppliedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MatchingService {

    private final MatchingManager matchingManager;
    private final MatchingReader matchingReader;
    private final PaymentManager paymentManager;
    private final ApplicationEventPublisher eventPublisher;

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
        eventPublisher.publishEvent(new MatchingAppliedEvent(matchingApplication.getId()));
    }

    /**
     * 회원의 매칭 신청 내역을 조회한다.
     * @param memberId 회원 식별자
     * @param status 조회할 신청 상태 (예: PENDING, APPROVED, REJECTED)
     *
     * @return 해당 회원의 매칭 신청 내역 리스트
     * */
    public List<MatchingApplication> getMyApplications(Long memberId, ApplicationStatus status) {
        return matchingReader.findByMemberAndStatus(memberId, status);
    }

    /**
     * 특정 매칭 신청에 대해 승인된 매칭 결과를 조회한다.
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     *
     * @return 해당 매칭 신청에 승인된 매칭 결과 리스트
     * */
    public List<MatchingResult> getApprovedApplication(Long applicationId, Long memberId) {
        return matchingReader.findApprovedByApplication(applicationId, memberId);
    }

    /**
     * 매칭 신청을 철회한다.
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     *
     * */
    public void withdraw(Long applicationId, Long memberId) {
        matchingManager.withdraw(applicationId, memberId);
    }

}