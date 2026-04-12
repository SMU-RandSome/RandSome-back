package org.smu.randsome.randsomeback.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventJpaRepository extends JpaRepository<CouponEvent, Long> {

    Optional<CouponEvent> findByIdAndStatus(Long id, EntityStatus status);
    List<CouponEvent> findAllByStatusOrderByStartsAtDesc(EntityStatus status);
    List<CouponEvent> findAllByEventStatusAndStartsAtLessThanEqualAndStatus(
            CouponEventStatus eventStatus,
            LocalDateTime now,
            EntityStatus status
    );
    List<CouponEvent> findAllByEventStatusAndExpiresAtLessThanEqualAndStatus(
            CouponEventStatus eventStatus,
            LocalDateTime now,
            EntityStatus status
    );

}