package org.smu.randsome.randsomeback.domain.qr.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class QrUsageManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    QrUsageManager qrUsageManager;

    @Mock
    RedisRepository redisRepository;

    @Test
    void tryConsume_처음_소비하면_true를_반환한다() {
        // given
        given(redisRepository.tryAcquire(any(), any())).willReturn(true);

        // when
        boolean result = qrUsageManager.tryConsume("uuid-1");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void tryConsume_이미_소비된_jti면_false를_반환한다() {
        // given
        given(redisRepository.tryAcquire(any(), any())).willReturn(false);

        // when
        boolean result = qrUsageManager.tryConsume("uuid-1");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void markAsUsed_호출_시_올바른_키로_Redis에_저장된다() {
        // when
        qrUsageManager.markAsUsed("uuid-1");

        // then
        then(redisRepository).should().put(eq("qr:used:uuid-1"), eq("1"), any(Duration.class));
    }

    @Test
    void findActiveJti_활성_jti가_없으면_empty를_반환한다() {
        // given
        given(redisRepository.get(any())).willReturn(null);

        // when & then
        assertThat(qrUsageManager.findActiveJti(1L)).isEmpty();
    }

    @Test
    void findActiveJti_활성_jti가_있으면_반환한다() {
        // given
        given(redisRepository.get("qr:active:1")).willReturn("uuid-1");

        // when & then
        assertThat(qrUsageManager.findActiveJti(1L)).hasValue("uuid-1");
    }

    @Test
    void registerActiveJti_호출_시_올바른_키로_Redis에_저장된다() {
        // when
        qrUsageManager.registerActiveJti(1L, "uuid-1");

        // then
        then(redisRepository).should().put(eq("qr:active:1"), eq("uuid-1"), any(Duration.class));
    }

}
