package org.smu.randsome.randsomeback.domain.candidate.implement;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class CandidateReader {

    private final CandidateRepository candidateRepository;

    public Optional<RegistrationStatus> findLatestRegistrationStatus(Long memberId) {
        return candidateRepository.findLatestRegistrationStatusByMemberId(memberId, EntityStatus.ACTIVE);
    }

    public CursorSlice<CandidateRegistration> findAllByFilter(
            CandidateRegistrationSearchCondition condition,
            Cursor cursor
    ) {
        List<CandidateRegistration> registrations = candidateRepository.findAllByFilter(
                condition,
                cursor.lastCursorId(),
                cursor.limit()
        );

        boolean hasNext = registrations.size() > cursor.limit();
        List<CandidateRegistration> items = hasNext ? registrations.subList(0, cursor.limit()) : registrations;
        Long nextCursor = hasNext ? items.getLast().getId() : null;

        return CursorSlice.of(items, nextCursor, hasNext);
    }

    public long countPending() {
        return candidateRepository.countByRegistrationStatusAndStatus(RegistrationStatus.PENDING, EntityStatus.ACTIVE);
    }

    public CandidateRegistration findWithMember(Long candidateRegistrationId) {
        return candidateRepository.findByIdAndStatusWithMember(candidateRegistrationId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION));
    }

}
