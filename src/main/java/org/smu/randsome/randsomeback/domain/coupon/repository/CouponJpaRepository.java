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
import org.springframework.stereotype.Repository;

@Repository
public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    @Query("""
            SELECT c FROM Coupon c
            JOIN FETCH c.couponEvent
            WHERE c.id = :id
              AND c.status = :status
            """)
    Optional<Coupon> findByIdAndStatusWithEvent(
            @Param("id") Long id,
            @Param("status") EntityStatus status
    );

    @Query("""
            SELECT c FROM Coupon c
            WHERE c.couponStatus = :couponStatus
              AND c.expiredAt < :now
              AND c.status = :entityStatus
            """)
    List<Coupon> findAllExpirable(
            @Param("couponStatus") CouponStatus couponStatus,
            @Param("now") LocalDateTime now,
            @Param("entityStatus") EntityStatus entityStatus
    );

}
