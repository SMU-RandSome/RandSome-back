package org.smu.randsome.randsomeback.domain.matching.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingAppliedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingService {

    private final MatchingManager matchingManager;
    private final MatchingReader matchingReader;
    private final TicketHandler ticketHandler;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 매칭 신청을 처리한다. 신청 시 회원의 티켓을 차감하고, 매칭 신청 정보를 저장한 후, 자동으로 매칭을 진행한다.
     *
     * @param newMatching 매칭 신청 커맨드
     * @param memberId 신청자 식별자
     */
    @Transactional
    public void apply(NewMatching newMatching, Long memberId) {
        ticketHandler.deduct(memberId, newMatching);

        MatchingApplication matchingApplication = matchingManager.apply(newMatching, memberId);
        matchingManager.approve(matchingApplication.getId(), LocalDateTime.now());

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
     * 매칭 신청을 취소한다.
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     *
     * */
    @Transactional
    public void cancel(Long applicationId, Long memberId) {
        matchingManager.cancel(applicationId, memberId);

        log.info("[MatchingService] 매칭 신청 취소 처리 완료 - applicationId: {}, memberId: {}",
                applicationId, memberId);
    }

    /**
     * 회원이 후보자로 노출된 횟수를 조회한다.
     * @param memberId 회원 식별자
     * @return 회원이 후보자로 노출된 총 횟수
     * */
    public long getExposureCount(Long memberId) {
        return matchingReader.countExposures(memberId);
    }

}