package org.smu.randsome.randsomeback.domain.qr.implement;

import static org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider.*;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class QrVerificationManager {

    private final QrTokenProvider qrTokenProvider;
    private final QrUsageManager qrUsageManager;

    /**
     * QR 토큰을 검증하고 회원 ID를 반환한다.
     *
     * @param token 검증할 QR JWT 토큰
     * @return QR 토큰에 포함된 회원 ID
     * @throws CoreException {@link ErrorType#INVALID_QR_TOKEN} - 토큰 형식/서명 오류
     * @throws CoreException {@link ErrorType#QR_TOKEN_EXPIRED} - 토큰 만료
     * @throws CoreException {@link ErrorType#QR_TOKEN_ALREADY_USED} - 이미 소비된 QR
     */
    public Long verifyAndGetMemberId(String token) {
        ParsedQrToken parsed = qrTokenProvider.parse(token);

        if (!qrUsageManager.tryConsume(parsed.jti())) {
            throw new CoreException(ErrorType.QR_TOKEN_ALREADY_USED);
        }

        return parsed.memberId();
    }

}
