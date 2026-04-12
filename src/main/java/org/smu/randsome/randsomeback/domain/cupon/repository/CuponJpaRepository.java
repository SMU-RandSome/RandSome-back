package org.smu.randsome.randsomeback.domain.cupon.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.cupon.entity.Cupon;
import org.smu.randsome.randsomeback.domain.cupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuponJpaRepository extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByIdAndStatus(Long id, EntityStatus status);

    List<Cupon> findAllByMemberIdAndStatus(Long memberId, EntityStatus status);

    @Query("""
            SELECT c FROM Cupon c
            JOIN FETCH c.couponEvent ce
            WHERE c.couponStatus = :couponStatus
              AND ce.expiresAt < :now
              AND c.status = :entityStatus
            """)
    List<Cupon> findAllExpirable(
            @Param("couponStatus") CouponStatus couponStatus,
            @Param("now") LocalDateTime now,
            @Param("entityStatus") EntityStatus entityStatus
    );

}
