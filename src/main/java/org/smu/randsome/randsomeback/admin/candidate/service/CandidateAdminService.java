package org.smu.randsome.randsomeback.admin.candidate.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CandidateAdminService {

    private final CandidateManager candidateManager;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 후보자 등록 승인 <br>
     * 승인된 후보자 등록은 매칭 대상이 됨 <br>
     * 승인 시 후보자 등록 승인 이벤트가 발행되어 피드 기록 등 후속 작업이 트랜잭션 커밋 이후 별도 트랜잭션으로 안전하게 처리됨 <br>
     * @param candidateRegistrationId 승인할 후보자 등록 ID
     * */
    public void approve(Long candidateRegistrationId) {
        CandidateRegistration candidateRegistration = candidateManager.approve(candidateRegistrationId);

        eventPublisher.publishEvent(new CandidateRegistrationApprovedEvent(candidateRegistration.getMember().getNickname()));
    }

    /**
     * 후보자 등록 거절 <br>
     * 거절된 후보자 등록은 매칭 대상에서 제외됨 <br>
     * @param candidateRegistrationId 거절할 후보자 등록 ID
     * @param rejectedReason 거절 사유 (관리자 입력)
     **/
    public void reject(Long candidateRegistrationId, String rejectedReason) {
        candidateManager.reject(candidateRegistrationId, rejectedReason);
    }

}