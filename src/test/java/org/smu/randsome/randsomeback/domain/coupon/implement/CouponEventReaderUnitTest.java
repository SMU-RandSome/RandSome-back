package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    void DRAFT_이벤트는_총_수량을_남은_수량으로_반환한다() {
        // given
        CouponEvent draftEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(draftEvent, "id", 1L);

        // when
        Map<Long, Long> result = couponEventReader.findRemainingStocks(List.of(draftEvent));

        // then
        assertThat(result.get(1L)).isEqualTo(CuponFixture.CUPON_QUANTITY);
        verify(redisRepository, never()).mget(anyList());
    }

    @Test
    void ACTIVE_이벤트들은_MGET으로_한_번에_Redis에서_남은_수량을_조회한다() {
        // given
        CouponEvent active1 = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(active1, "id", 1L);

        CouponEvent active2 = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(active2, "id", 2L);

        List<String> keys = List.of(CacheKeys.couponStock(1L), CacheKeys.couponStock(2L));
        given(redisRepository.mget(keys)).willReturn(List.of("7", "3"));

        // when
        Map<Long, Long> result = couponEventReader.findRemainingStocks(List.of(active1, active2));

        // then
        assertThat(result.get(1L)).isEqualTo(7L);
        assertThat(result.get(2L)).isEqualTo(3L);
        verify(redisRepository).mget(keys);
    }

    @Test
    void ACTIVE_이벤트의_Redis_값이_null이면_0을_반환한다() {
        // given
        CouponEvent activeEvent = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(activeEvent, "id", 1L);

        List<String> keys = List.of(CacheKeys.couponStock(1L));
        given(redisRepository.mget(keys)).willReturn(Arrays.asList(new String[]{null}));

        // when
        Map<Long, Long> result = couponEventReader.findRemainingStocks(List.of(activeEvent));

        // then
        assertThat(result.get(1L)).isEqualTo(0L);
    }

    @Test
    void SOLD_OUT_이벤트는_0을_반환한다() {
        // given
        CouponEvent soldOutEvent = CuponFixture.createActiveCuponEvent();
        soldOutEvent.soldOut();
        ReflectionTestUtils.setField(soldOutEvent, "id", 1L);

        // when
        Map<Long, Long> result = couponEventReader.findRemainingStocks(List.of(soldOutEvent));

        // then
        assertThat(result.get(1L)).isEqualTo(0L);
    }

    @Test
    void ENDED_이벤트는_0을_반환한다() {
        // given
        CouponEvent endedEvent = CuponFixture.createActiveCuponEvent();
        endedEvent.end();
        ReflectionTestUtils.setField(endedEvent, "id", 1L);

        // when
        Map<Long, Long> result = couponEventReader.findRemainingStocks(List.of(endedEvent));

        // then
        assertThat(result.get(1L)).isEqualTo(0L);
    }

    @Test
    void 혼합_상태의_이벤트_목록에서_ACTIVE만_MGET으로_조회한다() {
        // given
        CouponEvent draftEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(draftEvent, "id", 1L);

        CouponEvent activeEvent = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(activeEvent, "id", 2L);

        CouponEvent soldOutEvent = CuponFixture.createActiveCuponEvent();
        soldOutEvent.soldOut();
        ReflectionTestUtils.setField(soldOutEvent, "id", 3L);

        List<String> activeKeys = List.of(CacheKeys.couponStock(2L));
        given(redisRepository.mget(activeKeys)).willReturn(List.of("5"));

        // when
        Map<Long, Long> result = couponEventReader.findRemainingStocks(
                List.of(draftEvent, activeEvent, soldOutEvent));

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(1L)).isEqualTo(CuponFixture.CUPON_QUANTITY);
        assertThat(result.get(2L)).isEqualTo(5L);
        assertThat(result.get(3L)).isEqualTo(0L);
        verify(redisRepository).mget(activeKeys);
    }

}
