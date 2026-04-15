package org.smu.randsome.randsomeback.admin.candidate.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CandidateAdminService {

    private final CandidateManager candidateManager;

    /**
     * 후보자 등록 승인 <br>
     * 승인된 후보자 등록은 매칭 대상이 됨 <br>
     * @param candidateRegistrationId 승인할 후보자 등록 ID
     * */
    public void approve(Long candidateRegistrationId) {
        candidateManager.approve(candidateRegistrationId);
    }

}