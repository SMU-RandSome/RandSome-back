package org.smu.randsome.randsomeback.domain.coupon.repository;

import static org.smu.randsome.randsomeback.domain.coupon.entity.QCoupon.coupon;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponQueryDslRepositoryImpl implements CouponQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Coupon> findByMemberAndFilter(Long memberId, CouponSearchCondition condition) {
        return queryFactory
                .selectFrom(coupon)
                .innerJoin(coupon.couponEvent).fetchJoin()
                .where(
                        coupon.member.id.eq(memberId),
                        coupon.status.eq(EntityStatus.ACTIVE),
                        cursorCondition(condition.lastCouponId()),
                        getStatusFilter(condition.filter())
                )
                .orderBy(coupon.id.desc())
                .limit(condition.size() + 1L)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return coupon.id.lt(cursor);
    }

    private BooleanExpression getStatusFilter(CouponFilterType filter) {
        return switch (filter) {
            case AVAILABLE -> coupon.couponStatus.eq(CouponStatus.AVAILABLE);
            case USED_OR_EXPIRED -> coupon.couponStatus.in(CouponStatus.USED, CouponStatus.EXPIRED);
            case ALL -> null;
        };
    }

}