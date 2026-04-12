package org.smu.randsome.randsomeback.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuponJpaRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByIdAndStatus(Long id, EntityStatus status);

    List<Coupon> findAllByMemberIdAndStatus(Long memberId, EntityStatus status);

    @Query("""
            SELECT c FROM Coupon c
            JOIN FETCH c.couponEvent ce
            WHERE c.couponStatus = :couponStatus
              AND ce.expiresAt < :now
              AND c.status = :entityStatus
            """)
    List<Coupon> findAllExpirable(
            @Param("couponStatus") CouponStatus couponStatus,
            @Param("now") LocalDateTime now,
            @Param("entityStatus") EntityStatus entityStatus
    );

}
