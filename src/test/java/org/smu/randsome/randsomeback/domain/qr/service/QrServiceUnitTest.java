package org.smu.randsome.randsomeback.domain.qr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.qr.implement.QrImageGenerator;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider.CreatedQrToken;
import org.smu.randsome.randsomeback.domain.qr.implement.QrUsageManager;

class QrServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    QrService qrService;

    @Mock
    QrTokenProvider qrTokenProvider;

    @Mock
    QrImageGenerator qrImageGenerator;

    @Mock
    QrUsageManager qrUsageManager;

    @Test
    void QR_생성에_성공한다() {
        // given
        Long memberId = 1L;
        byte[] expectedImage = new byte[]{1, 2, 3};
        given(qrUsageManager.findActiveJti(memberId)).willReturn(Optional.empty());
        given(qrTokenProvider.createToken(memberId)).willReturn(new CreatedQrToken("token", "uuid-1"));
        given(qrImageGenerator.generate("token")).willReturn(expectedImage);

        // when
        byte[] result = qrService.generateMemberQr(memberId);

        // then
        assertThat(result).isEqualTo(expectedImage);
    }

    @Test
    void 이전_활성_QR이_없으면_무효화를_시도하지_않는다() {
        // given
        Long memberId = 1L;
        given(qrUsageManager.findActiveJti(memberId)).willReturn(Optional.empty());
        given(qrTokenProvider.createToken(memberId)).willReturn(new CreatedQrToken("token", "uuid-1"));
        given(qrImageGenerator.generate(any())).willReturn(new byte[0]);

        // when
        qrService.generateMemberQr(memberId);

        // then
        then(qrUsageManager).should(never()).markAsUsed(any());
    }

    @Test
    void 이전_활성_QR이_있으면_즉시_무효화한다() {
        // given
        Long memberId = 1L;
        String previousJti = "old-uuid";
        given(qrUsageManager.findActiveJti(memberId)).willReturn(Optional.of(previousJti));
        given(qrTokenProvider.createToken(memberId)).willReturn(new CreatedQrToken("token", "new-uuid"));
        given(qrImageGenerator.generate(any())).willReturn(new byte[0]);

        // when
        qrService.generateMemberQr(memberId);

        // then
        then(qrUsageManager).should().markAsUsed(previousJti);
    }

    @Test
    void 새_QR_발급_후_새_jti가_활성으로_등록된다() {
        // given
        Long memberId = 1L;
        String newJti = "new-uuid";
        given(qrUsageManager.findActiveJti(memberId)).willReturn(Optional.empty());
        given(qrTokenProvider.createToken(memberId)).willReturn(new CreatedQrToken("token", newJti));
        given(qrImageGenerator.generate(any())).willReturn(new byte[0]);

        // when
        qrService.generateMemberQr(memberId);

        // then
        then(qrUsageManager).should().registerActiveJti(memberId, newJti);
    }

}