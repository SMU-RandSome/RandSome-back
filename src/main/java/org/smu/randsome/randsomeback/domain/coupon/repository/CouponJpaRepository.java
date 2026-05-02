package org.smu.randsome.randsomeback.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Coupon c
            SET c.couponStatus = :expiredStatus,
                c.version = c.version + 1,
                c.updatedAt = :now
            WHERE c.couponStatus = :availableStatus
              AND c.expiredAt < :now
              AND c.status = :entityStatus
              AND c.usedAt IS NULL
            """)
    int bulkExpire(
            @Param("availableStatus") CouponStatus availableStatus,
            @Param("expiredStatus") CouponStatus expiredStatus,
            @Param("now") LocalDateTime now,
            @Param("entityStatus") EntityStatus entityStatus
    );

}
