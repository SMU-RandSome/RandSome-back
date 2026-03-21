package org.smu.randsome.randsomeback.domain.payment.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.QCandidateRegistration;
import org.smu.randsome.randsomeback.domain.matching.entity.QMatchingApplication;
import org.smu.randsome.randsomeback.domain.member.entity.QMember;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearch;
import org.smu.randsome.randsomeback.domain.payment.entity.QPayment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Repository
public class PaymentQueryRepositoryImpl implements PaymentQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QPayment payment = QPayment.payment;
    private static final QMember member = QMember.member;
    private static final QMatchingApplication matchingApplication = QMatchingApplication.matchingApplication;
    private static final QCandidateRegistration candidateRegistration = QCandidateRegistration.candidateRegistration;

    @Override
    public Page<PaymentWithReason> findPaymentsWithRejectedReason(
            PaymentSearch paymentSearch,
            Pageable pageable
    ) {
        List<PaymentWithReason> content = queryFactory
                .select(payment, matchingApplication.rejectedReason, candidateRegistration.rejectedReason)
                .from(payment)
                .join(payment.member, member).fetchJoin()
                .leftJoin(matchingApplication)
                .on(
                        matchingApplication.id.eq(payment.referenceId),
                        payment.paymentType.in(PaymentType.RANDOM_MATCHING, PaymentType.IDEAL_TYPE_MATCHING)
                )
                .leftJoin(candidateRegistration)
                .on(
                        candidateRegistration.id.eq(payment.referenceId),
                        payment.paymentType.eq(PaymentType.CANDIDATE_REGISTRATION)
                )
                .where(
                        paymentStatusIn(paymentSearch.paymentStatuses()),
                        isStatusEq(EntityStatus.ACTIVE),
                        searchByKeyword(paymentSearch.query())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(payment.id.desc())
                .fetch()
                .stream()
                .map(tuple -> {
                    String reason = tuple.get(matchingApplication.rejectedReason) != null
                            ? tuple.get(matchingApplication.rejectedReason)
                            : tuple.get(candidateRegistration.rejectedReason);
                    return new PaymentWithReason(tuple.get(payment), reason);
                })
                .toList();

        Long total = queryFactory
                .select(payment.count())
                .from(payment)
                .join(payment.member, member)
                .where(
                        paymentStatusIn(paymentSearch.paymentStatuses()),
                        isStatusEq(EntityStatus.ACTIVE),
                        searchByKeyword(paymentSearch.query())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression isStatusEq(EntityStatus status) {
        return payment.status.eq(status);
    }

    private BooleanExpression paymentStatusIn(List<PaymentStatus> paymentStatuses) {
        return payment.paymentStatus.in(paymentStatuses);
    }

    private BooleanExpression searchByKeyword(String query) {
        if (!StringUtils.hasText(query)) {
            return null;
        }
        String trimmedQuery = query.trim();

        return member.legalName.containsIgnoreCase(trimmedQuery);
    }

}