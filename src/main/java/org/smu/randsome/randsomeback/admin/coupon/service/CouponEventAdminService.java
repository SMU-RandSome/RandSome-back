package org.smu.randsome.randsomeback.admin.coupon.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.UpdateCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponCacheManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponEventAdminService {

    private final CouponEventManager couponEventManager;
    private final CouponEventReader couponEventReader;
    private final CouponCacheManager couponCacheManager;

    /**
     * 쿠폰 이벤트 등록
     * @param newCouponEvent 쿠폰 이벤트 등록에 필요한 정보가 담긴 객체
     * @return 등록된 쿠폰 이벤트의 ID
     * */
    public Long registerCouponEvent(NewCouponEvent newCouponEvent) {
        CouponEvent event = couponEventManager.register(newCouponEvent);

        return event.getId();
    }

    /**
     * 쿠폰 이벤트 상세 조회
     * @param couponEventId 조회할 쿠폰 이벤트 ID
     * @return 조회된 쿠폰 이벤트
     * */
    public CouponEvent findCouponEvent(Long couponEventId) {
        return couponEventReader.find(couponEventId);
    }

    /**
     * 쿠폰 이벤트 수정
     * @param couponEventId 수정할 쿠폰 이벤트 ID
     * @param updateCouponEvent 수정할 정보가 담긴 객체
     * */
    public void updateCouponEvent(Long couponEventId, UpdateCouponEvent updateCouponEvent) {
        couponEventManager.update(couponEventId, updateCouponEvent);
    }

    /**
     * 쿠폰 이벤트 삭제 (소프트 삭제)
     * @param couponEventId 삭제할 쿠폰 이벤트 ID
     * */
    public void deleteCouponEvent(Long couponEventId) {
        couponEventManager.delete(couponEventId);
    }

    /**
     * 쿠폰 이벤트 목록 조회
     * @return 조회된 쿠폰 이벤트 리스트
     * */
    public List<CouponEvent> findCouponEvents() {
        return couponEventReader.findCouponEvents();
    }

    /**
     * 쿠폰 이벤트 활성화 및 Redis stock 초기화
     * @param couponEventId 활성화할 쿠폰 이벤트 ID
     * */
    @Transactional
    public void activateCouponEvent(Long couponEventId) {
        CouponEvent event = couponEventManager.activate(couponEventId);

        Duration ttl = Duration.between(LocalDateTime.now(), event.getExpiresAt());
        couponCacheManager.initializeStock(couponEventId, event.getTotalQuantity(), ttl);
    }

}