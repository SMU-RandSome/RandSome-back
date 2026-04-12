package org.smu.randsome.randsomeback.admin.coupon.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponEventAdminService {

    private final CouponEventManager couponEventManager;

    /**
     * 쿠폰 이벤트 등록
     * @param newCouponEvent 쿠폰 이벤트 등록에 필요한 정보가 담긴 객체
     * @return 등록된 쿠폰 이벤트의 ID
     * */
    public Long registerCouponEvent(NewCouponEvent newCouponEvent) {
        CouponEvent event = couponEventManager.register(newCouponEvent);

        return event.getId();
    }

}