package org.smu.randsome.randsomeback.domain.qr.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider.CreatedQrToken;
import org.smu.randsome.randsomeback.domain.qr.implement.QrUsageManager;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
class QrServiceIntegrationTest extends IntegrationTestSupport {

    // PNG 파일 시그니처 (첫 4바이트)
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};

    final QrService qrService;
    final QrTokenProvider qrTokenProvider;
    final QrUsageManager qrUsageManager;
    final StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        Set<String> keys = stringRedisTemplate.keys("qr:*");
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    void QR_생성_결과가_실제_PNG_이미지이다() {
        // when
        byte[] qrImage = qrService.generateMemberQr(1L);

        // then
        assertThat(qrImage).isNotEmpty();
        assertThat(qrImage[0]).isEqualTo(PNG_SIGNATURE[0]);
        assertThat(qrImage[1]).isEqualTo(PNG_SIGNATURE[1]);
        assertThat(qrImage[2]).isEqualTo(PNG_SIGNATURE[2]);
        assertThat(qrImage[3]).isEqualTo(PNG_SIGNATURE[3]);
    }

    @Test
    void QR_안의_토큰을_파싱하면_올바른_memberId가_담겨있다() {
        // given
        Long memberId = 42L;
        CreatedQrToken created = qrTokenProvider.createToken(memberId);

        // when
        QrTokenProvider.ParsedQrToken parsed = qrTokenProvider.parse(created.token());

        // then
        assertThat(parsed.memberId()).isEqualTo(memberId);
    }

    @Test
    void 새_QR_발급_시_이전_QR이_무효화된다() {
        // given
        Long memberId = 1L;
        CreatedQrToken first = qrTokenProvider.createToken(memberId);
        qrUsageManager.registerActiveJti(memberId, first.jti());

        // when - 새 QR 발급
        qrService.generateMemberQr(memberId);

        // then - 이전 jti는 사용됨 처리, tryConsume은 false 반환
        assertThat(qrUsageManager.tryConsume(first.jti())).isFalse();
    }

    @Test
    void 새_QR_발급_후_새_jti가_Redis_active에_등록된다() {
        // given
        Long memberId = 1L;

        // when
        qrService.generateMemberQr(memberId);

        // then
        assertThat(qrUsageManager.findActiveJti(memberId)).isPresent();
    }

    @Test
    void 같은_jti로_동시에_10개_요청_시_정확히_1개만_성공한다() throws InterruptedException {
        // given
        CreatedQrToken created = qrTokenProvider.createToken(1L);
        String jti = created.jti();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                boolean result = qrUsageManager.tryConsume(jti);
                doneLatch.countDown();
                return result;
            }));
        }

        // when - 모든 스레드 동시 시작
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then - 정확히 1개만 성공
        long successCount = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(Boolean::booleanValue)
                .count();

        assertThat(successCount).isEqualTo(1);
    }

}