package org.smu.randsome.randsomeback.domain.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.QCandidateRegistration;
import org.smu.randsome.randsomeback.domain.matching.entity.QMatchingApplication;
import org.smu.randsome.randsomeback.domain.member.entity.QMember;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.entity.QPayment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
            List<PaymentStatus> paymentStatuses,
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
                    payment.paymentStatus.in(paymentStatuses),
                    payment.status.eq(EntityStatus.ACTIVE)
                )
                .orderBy(payment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
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
                .where(
                    payment.paymentStatus.in(paymentStatuses),
                    payment.status.eq(EntityStatus.ACTIVE)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

}