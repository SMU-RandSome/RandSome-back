package org.smu.randsome.randsomeback.domain.cupon.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.cupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventJpaRepository extends JpaRepository<CouponEvent, Long> {

    Optional<CouponEvent> findByIdAndStatus(Long id, EntityStatus status);

}