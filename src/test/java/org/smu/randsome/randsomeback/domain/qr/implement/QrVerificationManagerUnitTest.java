package org.smu.randsome.randsomeback.domain.qr.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class QrVerificationManagerUnitTest {

    @Mock
    QrTokenProvider qrTokenProvider;

    @Mock
    QrUsageManager qrUsageManager;

    @InjectMocks
    QrVerificationManager qrVerificationManager;

    @Test
    void QR_검증에_성공하면_memberId를_반환한다() {
        // given
        String token = "valid.token";
        Long memberId = 1L;
        String jti = "test-jti";

        given(qrTokenProvider.parse(token)).willReturn(new QrTokenProvider.ParsedQrToken(memberId, jti));
        given(qrUsageManager.tryConsume(jti)).willReturn(true);

        // when
        Long result = qrVerificationManager.verifyAndGetMemberId(token);

        // then
        assertThat(result).isEqualTo(memberId);
    }

    @Test
    void QR토큰_파싱_실패_시_예외를_던진다() {
        // given
        String token = "invalid.token";
        given(qrTokenProvider.parse(token)).willThrow(new CoreException(ErrorType.INVALID_QR_TOKEN));

        // when & then
        assertThatThrownBy(() -> qrVerificationManager.verifyAndGetMemberId(token))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.INVALID_QR_TOKEN));

        verify(qrUsageManager, never()).tryConsume(token);
    }

    @Test
    void QR토큰_만료_시_예외를_던진다() {
        // given
        String token = "expired.token";
        given(qrTokenProvider.parse(token)).willThrow(new CoreException(ErrorType.QR_TOKEN_EXPIRED));

        // when & then
        assertThatThrownBy(() -> qrVerificationManager.verifyAndGetMemberId(token))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.QR_TOKEN_EXPIRED));

        verify(qrUsageManager, never()).tryConsume(token);
    }

    @Test
    void 이미_소비된_QR토큰이면_예외를_던진다() {
        // given
        String token = "already-used.token";
        String jti = "consumed-jti";

        given(qrTokenProvider.parse(token)).willReturn(new QrTokenProvider.ParsedQrToken(1L, jti));
        given(qrUsageManager.tryConsume(jti)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> qrVerificationManager.verifyAndGetMemberId(token))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.QR_TOKEN_ALREADY_USED));
    }

}