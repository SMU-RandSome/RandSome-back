package org.smu.randsome.randsomeback.domain.candidate.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;

public interface CandidateQueryDslRepository {

    List<CandidateRegistration> findAllByFilter(CandidateRegistrationSearchCondition condition, Long lastId, int size);

}
