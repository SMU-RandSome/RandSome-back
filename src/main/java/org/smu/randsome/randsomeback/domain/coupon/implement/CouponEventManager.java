package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventActivatedEvent;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.UpdateCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponEventManager {

    private final CouponEventJpaRepository couponEventJpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CouponEvent register(NewCouponEvent newCouponEvent) {
        CouponEvent couponEvent = CouponEvent.create(
                newCouponEvent.name(),
                newCouponEvent.description(),
                newCouponEvent.type(),
                newCouponEvent.totalQuantity(),
                newCouponEvent.rewardTicketType(),
                newCouponEvent.rewardTicketAmount(),
                newCouponEvent.startsAt(),
                newCouponEvent.expiresAt(),
                newCouponEvent.couponExpiresAt()
        );
        return couponEventJpaRepository.save(couponEvent);
    }

    @Transactional
    public void update(Long couponEventId, UpdateCouponEvent updateCouponEvent) {
        CouponEvent event = couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        event.update(
                updateCouponEvent.name(),
                updateCouponEvent.description(),
                updateCouponEvent.type(),
                updateCouponEvent.totalQuantity(),
                updateCouponEvent.rewardTicketType(),
                updateCouponEvent.rewardTicketAmount(),
                updateCouponEvent.startsAt(),
                updateCouponEvent.expiresAt(),
                updateCouponEvent.couponExpiresAt()
        );
    }

    @Transactional
    public void delete(Long couponEventId) {
        CouponEvent event = couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        event.delete();
    }

    /**
     * 쿠폰 이벤트 활성화 <br>
     * - 이벤트 상태를 활성화로 변경한다. <br>
     * - 커밋 후 CouponEventActivatedEvent를 발행하여 Redis에 재고 초기화 및 락 설정을 트리거한다. <br>
     * */
    @Transactional
    public void activate(Long couponEventId) {
        CouponEvent event = couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        event.activate(LocalDateTime.now());

        eventPublisher.publishEvent(new CouponEventActivatedEvent(event.getId(), event.getTotalQuantity(), event.getExpiresAt()));
    }

    @Transactional
    public void deactivate(Long couponEventId) {
        CouponEvent event = couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        event.end();
    }

}