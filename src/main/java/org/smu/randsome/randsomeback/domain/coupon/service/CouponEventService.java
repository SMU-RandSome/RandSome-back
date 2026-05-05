package org.smu.randsome.randsomeback.domain.coupon.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponManager;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponEventService {

    private final CouponManager couponManager;
    private final CouponEventReader couponEventReader;

     /**
      * 쿠폰 이벤트로부터 쿠폰을 발급하는 서비스 메서드입니다. <br>
      * 해당 메서드는 쿠폰 이벤트가 활성 상태인지 검증한 후, 쿠폰을 발급합니다. <br>
      * 쿠폰 발급 과정에서 Redis 기반의 동시성 제어가 적용되어, 동일한 이벤트에 대해 여러 회원이 동시에 쿠폰을 발급받는 경우에도 재고가 정확하게 관리됩니다.
      * @param couponEventId 발급할 쿠폰 이벤트의 ID
      * @param memberId 쿠폰을 발급받는 회원의 ID
      *
      * @return 발급된 쿠폰의 ID
      * */
    public Long publishCouponFromEvent(Long couponEventId, Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        return couponManager.issueCoupon(couponEventId, memberId, now);
    }

     /**
      * 모든 쿠폰 이벤트를 조회하는 서비스 메서드입니다. <br>
      * 해당 메서드는 활성 상태인 쿠폰 이벤트를 시작일 기준으로 내림차순으로 조회하여 반환합니다.
      * */
    public List<CouponEvent> findCouponEvents() {
        return couponEventReader.findCouponEvents();
    }

    /**
     * 쿠폰 이벤트 상세 조회
     * @param couponEventId 조회할 쿠폰 이벤트 ID
     * @return 조회된 쿠폰 이벤트
     * */
    public CouponEvent findCouponEvent(Long couponEventId) {
        return couponEventReader.find(couponEventId);
    }

}