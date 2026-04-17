package org.smu.randsome.randsomeback.admin.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

@RequiredArgsConstructor
class CouponEventActivationIntegrationTest extends IntegrationTestSupport {

    private final CouponEventAdminService couponEventAdminService;
    private final CouponEventJpaRepository couponEventJpaRepository;
    private final RedisRepository redisRepository;

    @AfterEach
    void tearDown() {
        couponEventJpaRepository.deleteAll();
    }

    @Test
    void 쿠폰_이벤트_활성화_시_DB_상태가_ACTIVE로_변경되고_Redis에_재고가_초기화된다() {
        // given
        CouponEvent event = couponEventJpaRepository.save(CuponFixture.createCuponEvent());

        // when: @Transactional 서비스 호출 → 커밋 → AFTER_COMMIT 리스너가 Redis 초기화
        couponEventAdminService.activateCouponEvent(event.getId());

        // then: DB 상태 확인
        CouponEvent activated = couponEventJpaRepository.findById(event.getId()).orElseThrow();
        assertThat(activated.getEventStatus()).isEqualTo(CouponEventStatus.ACTIVE);

        // then: Redis 재고 초기화 확인
        String stock = redisRepository.get(CacheKeys.couponStock(event.getId()));
        assertThat(stock).isEqualTo(String.valueOf(CuponFixture.CUPON_QUANTITY));
    }

    @Test
    void 트랜잭션_롤백_시_AFTER_COMMIT_리스너가_실행되지_않아_Redis에_재고가_초기화되지_않는다() {
        // given: 존재하지 않는 ID → activate() 내부에서 예외 발생 → 트랜잭션 롤백
        Long nonExistentId = 99999L;

        // when & then: 예외 발생 확인
        assertThatThrownBy(() -> couponEventAdminService.activateCouponEvent(nonExistentId))
                .isInstanceOf(CoreException.class);

        // then: 커밋이 일어나지 않았으므로 AFTER_COMMIT 리스너가 실행되지 않아 Redis 키가 없어야 한다
        String stock = redisRepository.get(CacheKeys.couponStock(nonExistentId));
        assertThat(stock).isNull();
    }

    @Test
    void 쿠폰_이벤트_비활성화_시_DB_상태가_ENDED로_변경되고_Redis에서_재고가_삭제된다() {
        // given: ACTIVE 상태의 이벤트 활성화 후 비활성화
        CouponEvent event = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        couponEventAdminService.activateCouponEvent(event.getId());

        // Redis에 재고가 초기화되었는지 확인
        String initialStock = redisRepository.get(CacheKeys.couponStock(event.getId()));
        assertThat(initialStock).isEqualTo(String.valueOf(CuponFixture.CUPON_QUANTITY));

        // when: @Transactional 서비스 호출 → 커밋 → AFTER_COMMIT 리스너가 Redis 삭제
        couponEventAdminService.deactivateCouponEvent(event.getId());

        // then: DB 상태 확인
        CouponEvent deactivated = couponEventJpaRepository.findById(event.getId()).orElseThrow();
        assertThat(deactivated.getEventStatus()).isEqualTo(CouponEventStatus.ENDED);

        // then: Redis 재고 삭제 확인
        String stock = redisRepository.get(CacheKeys.couponStock(event.getId()));
        assertThat(stock).isNull();
    }

    @Test
    void 비활성화_트랜잭션_롤백_시_AFTER_COMMIT_리스너가_실행되지_않아_Redis에_재고가_남아있다() {
        // given: ACTIVE 상태의 이벤트 활성화
        CouponEvent event = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        couponEventAdminService.activateCouponEvent(event.getId());

        // Redis에 재고가 초기화되었는지 확인
        String initialStock = redisRepository.get(CacheKeys.couponStock(event.getId()));
        assertThat(initialStock).isEqualTo(String.valueOf(CuponFixture.CUPON_QUANTITY));

        // when & then: 존재하지 않는 ID → deactivate() 내부에서 예외 발생 → 트랜잭션 롤백
        Long nonExistentId = 99999L;
        assertThatThrownBy(() -> couponEventAdminService.deactivateCouponEvent(nonExistentId))
                .isInstanceOf(CoreException.class);

        // then: 커밋이 일어나지 않았으므로 AFTER_COMMIT 리스너가 실행되지 않아 Redis 재고가 여전히 있어야 한다
        String stock = redisRepository.get(CacheKeys.couponStock(event.getId()));
        assertThat(stock).isEqualTo(String.valueOf(CuponFixture.CUPON_QUANTITY));
    }

}