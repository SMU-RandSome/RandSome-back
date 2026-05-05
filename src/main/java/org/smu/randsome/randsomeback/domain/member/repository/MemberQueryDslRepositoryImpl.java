package org.smu.randsome.randsomeback.domain.member.repository;

import static org.smu.randsome.randsomeback.domain.member.entity.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSearchCondition;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberQueryDslRepositoryImpl implements MemberQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Member> findAllNonDeletedNonAdmin(MemberSearchCondition condition, long offset, long limit) {
        return queryFactory
                .selectFrom(member)
                .where(
                        member.status.ne(EntityStatus.DELETED),
                        member.role.ne(Role.ROLE_ADMIN),
                        keywordFilter(condition)
                )
                .orderBy(member.id.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public long countNonDeletedNonAdmin(MemberSearchCondition condition) {
        Long count = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        member.status.ne(EntityStatus.DELETED),
                        member.role.ne(Role.ROLE_ADMIN),
                        keywordFilter(condition)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression keywordFilter(MemberSearchCondition condition) {
        if (!condition.hasKeyword()) {
            return null;
        }
        String keyword = condition.keyword().trim();
        return member.nickname.containsIgnoreCase(keyword)
                .or(member.legalName.containsIgnoreCase(keyword));
    }

}
