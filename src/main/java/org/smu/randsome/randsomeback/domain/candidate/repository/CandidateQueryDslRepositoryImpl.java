package org.smu.randsome.randsomeback.domain.candidate.repository;

import static org.smu.randsome.randsomeback.domain.candidate.entity.QCandidateRegistration.candidateRegistration;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CandidateQueryDslRepositoryImpl implements CandidateQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CandidateRegistration> findAllByFilter(
            CandidateRegistrationSearchCondition condition,
            Long lastId,
            int size
    ) {
        return queryFactory.selectFrom(candidateRegistration)
                .innerJoin(candidateRegistration.member).fetchJoin()
                .where(
                        candidateRegistration.status.eq(EntityStatus.ACTIVE),
                        candidateRegistration.member.status.eq(EntityStatus.ACTIVE),
                        cursorCondition(lastId),
                        statusFilter(condition.filter()),
                        keywordFilter(condition.keyword())
                )
                .orderBy(candidateRegistration.id.desc())
                .limit(size + 1L)
                .fetch();

    }

    private BooleanExpression cursorCondition(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return candidateRegistration.id.lt(lastId);
    }

    private BooleanExpression statusFilter(CandidateRegistrationFilter filter) {
        if (filter == null) {
            return null;
        }
        return candidateRegistration.registrationStatus.in(filter.getStatuses());
    }

    private BooleanExpression keywordFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        keyword = keyword.trim();

        return candidateRegistration.member.nickname.containsIgnoreCase(keyword)
                .or(candidateRegistration.member.legalName.containsIgnoreCase(keyword));
    }

}
