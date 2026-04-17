package org.smu.randsome.randsomeback.domain.matching.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingService {

    private final MatchingManager matchingManager;
    private final MatchingReader matchingReader;
    private final TicketHandler ticketHandler;

    /**
     * 매칭 신청을 처리한다.
     * <br/>신청 시 회원의 티켓을 차감하고, 매칭 신청을 생성한 후, 자동으로 매칭 알고리즘을 실행하여 결과를 생성한다.
     * <br/>트랜잭션 경계는 Service에서 관리되며, 티켓 차감과 매칭 생성/실행을 함께 처리한다.
     *
     * @param newMatching 매칭 신청 커맨드 (매칭 타입, 신청 인원, 이상형 조건 포함)
     * @param memberId 신청자 식별자
     */
    @Transactional
    public MatchingApplication apply(NewMatching newMatching, Long memberId) {
        ticketHandler.deduct(memberId, newMatching);

        MatchingApplication matchingApplication = matchingManager.apply(newMatching, memberId);
        matchingManager.executeMatching(matchingApplication, LocalDateTime.now());

        ticketHandler.refundForPartialMatch(
                memberId,
                newMatching.matchingType(),
                matchingApplication.getApplicationCount(),
                matchingApplication.getMatchedCount()
        );

        return matchingApplication;
    }

    /**
     * 매칭 신청을 취소한다.
     * <br/>`PENDING` 상태의 신청만 취소 가능하며, 이미 매칭된 신청(`SUCCESS`)은 취소할 수 없다.
     * <br/>신청자(memberId)의 신청이 맞는지 보안 검증을 포함한다.
     *
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     */
    @Transactional
    public void cancel(Long applicationId, Long memberId) {
        matchingManager.cancel(applicationId, memberId);

        log.info("[MatchingService] 매칭 신청 취소 처리 완료 - applicationId: {}, memberId: {}",
                applicationId, memberId);
    }

    /**
     * 회원이 신청한 매칭 리스트를 조회한다.
     * <br/>신청의 상태(`PENDING`, `SUCCESS`, `CANCELED`)와 신청 시각 등의 정보를 포함한다.
     * <br/>회원이 신청한 모든 매칭 신청을 반환하며, 필요 시 페이징이나 필터링 기능을 추가할 수 있다.
     * */
    public List<MatchingApplication> findMatchings(Long memberId) {
        return matchingReader.findMatchings(memberId);
    }

    /**
     * 특정 매칭 신청에 대해 매칭된 결과를 조회한다.
     * <br/>신청이 SUCCESS 상태일 때만 매칭 결과를 반환할 수 있다.
     * <br/>신청자(memberId)의 신청이 맞는지 보안 검증을 포함한다.
     *
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     * @return 해당 매칭 신청의 매칭 결과 리스트 (후보자 정보 포함)
     */
    public List<MatchingResult> findApplication(Long applicationId, Long memberId) {
        return matchingReader.findApplication(applicationId, memberId);
    }

    /**
     * 회원이 다른 사용자의 매칭 결과로 노출된 횟수를 조회한다.
     * <br/>즉, 이 회원을 후보자로 제시한 매칭 결과의 개수를 반환한다.
     *
     * @param memberId 회원 식별자
     * @return 회원이 후보자로 노출된 총 횟수
     */
    public long getExposureCount(Long memberId) {
        return matchingReader.countExposures(memberId);
    }

}