package org.smu.randsome.randsomeback.domain.candidate.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateReader;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateValidator;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
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
    private final PaymentManager paymentManager;

    @Transactional
    public void apply(Long memberId) {
        candidateValidator.validateApply(memberId);

        CandidateRegistration candidateRegistration = candidateManager.apply(memberId);

        paymentManager.register(
                candidateRegistration.getMember(),
                PaymentType.CANDIDATE_REGISTRATION,
                candidateRegistration.getId(),
                CANDIDATE_REGISTRATION_AMOUNT
        );

        log.info("[CandidateService] 매칭 후보자 등록 신청 완료 - memberId: {}", memberId);
    }

    public void withdraw(Long memberId) {
        candidateManager.withdraw(memberId);

        log.info("[CandidateService] 매칭 후보자 등록 철회 완료 - memberId: {}", memberId);
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

}