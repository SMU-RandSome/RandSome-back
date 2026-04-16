package org.smu.randsome.randsomeback.admin.candidate.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationNotificationEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateReader;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CandidateAdminService {

    private final CandidateManager candidateManager;
    private final CandidateReader candidateReader;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 후보자 등록 승인 <br>
     * 승인된 후보자 등록은 매칭 대상이 됨 <br>
     * 승인 시 후보자 등록 승인 이벤트가 발행되어 관련된 후속 작업이 트리거됨 (예: 승인 알림 발송 등) <br>
     * @param candidateRegistrationId 승인할 후보자 등록 ID
     * */
    public void approve(Long candidateRegistrationId) {
        CandidateRegistration candidateRegistration = candidateManager.approve(candidateRegistrationId);

        eventPublisher.publishEvent(new CandidateRegistrationNotificationEvent(candidateRegistration.getId(), NotificationType.CANDIDATE_APPROVED));
        eventPublisher.publishEvent(new CandidateRegistrationApprovedEvent(candidateRegistration.getMember().getNickname()));
    }

    /**
     * 후보자 등록 거절 <br>
     * 거절된 후보자 등록은 매칭 대상에서 제외됨 <br>
     * 거절 시 후보자 등록 거절 이벤트가 발행되어 관련된 후속 작업이 트리거됨 (예: 거절 알림 발송 등) <br>
     * @param candidateRegistrationId 거절할 후보자 등록 ID
     * @param rejectedReason 거절 사유 (관리자 입력)
     **/
    public void reject(Long candidateRegistrationId, String rejectedReason) {
        candidateManager.reject(candidateRegistrationId, rejectedReason);

        eventPublisher.publishEvent(new CandidateRegistrationNotificationEvent(candidateRegistrationId, NotificationType.CANDIDATE_REJECTED));

    }

    /**
     * 후보자 등록 신청 내역을 조회한다. </br>
     * 조회 조건에 따라 등록 상태, 검색 키워드 등을 필터링하여 결과를 반환한다. </br>
     * @param condition 조회 조건 (예: 등록 상태, 검색 키워드 등)
     * @param cursor    페이지네이션 정보 (마지막 조회 ID, 페이지 크기 등)
     * @return 후보자 등록 신청 내역 리스트와 다음 페이지 정보
     * */
    public CursorSlice<CandidateRegistration> findCandidates(
            CandidateRegistrationSearchCondition condition,
            Cursor cursor
    ) {
        return candidateReader.findAllByFilter(condition, cursor);
    }

}