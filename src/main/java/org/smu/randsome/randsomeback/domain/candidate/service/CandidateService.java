package org.smu.randsome.randsomeback.domain.candidate.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateReader;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CandidateService {

    public static final int CANDIDATE_REGISTRATION_AMOUNT = 1;

    private final CandidateValidator candidateValidator;
    private final CandidateManager candidateManager;
    private final CandidateReader candidateReader;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void apply(Long memberId) {
        candidateValidator.validateApply(memberId);
        CandidateRegistration candidateRegistration = candidateManager.apply(memberId);

        eventPublisher.publishEvent(new CandidateAppliedEvent(candidateRegistration.getId()));
    }

    /**
     * 회원이 매칭 후보자 등록을 철회하는 서비스 메서드입니다.
     * 후보자 에서 일반 회원으로 역할이 변경됩니다.
     * */
    public void withdraw(Long memberId) {
        candidateManager.withdraw(memberId);
    }

    /**
     * 회원의 최신 매칭 후보자 등록 상태를 조회하는 서비스 메서드입니다.
     *
     * @param memberId 회원 ID
     * @return 회원의 최신 매칭 후보자 등록 상태
     *
     */
    public Optional<RegistrationStatus> getMyRegistrationStatus(Long memberId) {
        return candidateReader.findLatestRegistrationStatus(memberId);
    }

    @Transactional
    public void cancel(Long memberId) {
        CandidateRegistration cancelled = candidateManager.cancel(memberId);

        log.info("[CandidateService] 후보자 등록 신청 취소 처리 완료 - registrationId={}, memberId={}",
                cancelled.getId(),
                memberId);
    }

}