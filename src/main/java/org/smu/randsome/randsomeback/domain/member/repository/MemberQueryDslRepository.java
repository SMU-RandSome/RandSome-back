package org.smu.randsome.randsomeback.domain.member.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.smu.randsome.randsomeback.domain.member.entity.QMember.member;

@RequiredArgsConstructor
@Repository
public class MemberQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Member> searchByNicknameOrLegalName(String query, Pageable pageable) {

        BooleanExpression searchCondition = hasSearchQuery(query);

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(
                        member.status.eq(EntityStatus.ACTIVE),
                        member.role.ne(Role.ROLE_ADMIN),
                        searchCondition
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        member.status.eq(EntityStatus.ACTIVE),
                        member.role.ne(Role.ROLE_ADMIN),
                        searchCondition
                )
                .fetchOne();

        return PageableExecutionUtils.getPage(results, pageable, () -> total != null ? total : 0L);
    }

    private BooleanExpression hasSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return member.nickname.containsIgnoreCase(query)
                .or(member.legalName.containsIgnoreCase(query));
    }

}
