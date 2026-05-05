package org.smu.randsome.randsomeback.domain.matching.repository;

import static org.smu.randsome.randsomeback.domain.matching.entity.QMatchingApplication.matchingApplication;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingSortType;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MatchingQueryDslRepositoryImpl implements MatchingQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MatchingApplication> findAllByFilter(MatchingSearchCondition condition, long offset, long limit) {
        return queryFactory.selectFrom(matchingApplication)
                .innerJoin(matchingApplication.member).fetchJoin()
                .where(
                        matchingApplication.member.status.eq(EntityStatus.ACTIVE),
                        genderFilter(condition.gender()),
                        dateFilter(condition.date()),
                        keywordFilter(condition.keyword()),
                        matchingApplication.status.eq(EntityStatus.ACTIVE)
                )
                .orderBy(sortOrder(condition.sort()))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public long countByFilter(MatchingSearchCondition condition) {
        Long count = queryFactory.select(matchingApplication.count())
                .from(matchingApplication)
                .innerJoin(matchingApplication.member)
                .where(
                        matchingApplication.member.status.eq(EntityStatus.ACTIVE),
                        genderFilter(condition.gender()),
                        dateFilter(condition.date()),
                        keywordFilter(condition.keyword()),
                        matchingApplication.status.eq(EntityStatus.ACTIVE)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression dateFilter(LocalDate date) {
        if (date == null) {
            return null;
        }
        return matchingApplication.createdAt.goe(date.atStartOfDay())
                .and(matchingApplication.createdAt.lt(date.plusDays(1).atStartOfDay()));
    }

    private BooleanExpression genderFilter(Gender gender) {
        if (gender == null) {
            return null;
        }
        return matchingApplication.member.gender.eq(gender);
    }

    private BooleanExpression keywordFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String trimmed = keyword.trim();
        return matchingApplication.member.nickname.containsIgnoreCase(trimmed)
                .or(matchingApplication.member.legalName.containsIgnoreCase(trimmed));
    }

    private OrderSpecifier<?> sortOrder(MatchingSortType sort) {
        return sort == MatchingSortType.OLDEST
                ? matchingApplication.id.asc()
                : matchingApplication.id.desc();
    }

}