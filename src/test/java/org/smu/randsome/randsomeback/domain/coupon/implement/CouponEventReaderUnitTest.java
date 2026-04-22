package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.test.util.ReflectionTestUtils;

class CouponEventReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventReader couponEventReader;

    @Mock
    CouponEventJpaRepository couponEventJpaRepository;

    @Mock
    RedisRepository redisRepository;

    @Test
    void DRAFT_상태의_이벤트는_총_수량을_남은_수량으로_반환한다() {
        // given
        CouponEvent event = CuponFixture.createCuponEvent();

        // when
        long remainingStock = couponEventReader.findRemainingStock(event);

        // then
        assertThat(remainingStock).isEqualTo(CuponFixture.CUPON_QUANTITY);
    }

    @Test
    void ACTIVE_상태의_이벤트는_Redis에서_남은_수량을_조회한다() {
        // given
        CouponEvent event = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(event, "id", 1L);
        given(redisRepository.get(CacheKeys.couponStock(1L)))
                .willReturn("7");

        // when
        long remainingStock = couponEventReader.findRemainingStock(event);

        // then
        assertThat(remainingStock).isEqualTo(7L);
    }

    @Test
    void ACTIVE_상태에서_Redis_값이_없으면_0을_반환한다() {
        // given
        CouponEvent event = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(event, "id", 1L);
        given(redisRepository.get(CacheKeys.couponStock(1L)))
                .willReturn(null);

        // when
        long remainingStock = couponEventReader.findRemainingStock(event);

        // then
        assertThat(remainingStock).isEqualTo(0L);
    }

    @Test
    void SOLD_OUT_상태의_이벤트는_0을_반환한다() {
        // given
        CouponEvent event = CuponFixture.createActiveCuponEvent();
        event.soldOut();

        // when
        long remainingStock = couponEventReader.findRemainingStock(event);

        // then
        assertThat(remainingStock).isEqualTo(0L);
    }

    @Test
    void ENDED_상태의_이벤트는_0을_반환한다() {
        // given
        CouponEvent event = CuponFixture.createActiveCuponEvent();
        event.end();

        // when
        long remainingStock = couponEventReader.findRemainingStock(event);

        // then
        assertThat(remainingStock).isEqualTo(0L);
    }

}
