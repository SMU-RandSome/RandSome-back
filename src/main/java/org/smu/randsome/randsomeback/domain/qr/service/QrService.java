package org.smu.randsome.randsomeback.domain.qr.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.qr.implement.QrImageGenerator;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider.CreatedQrToken;
import org.smu.randsome.randsomeback.domain.qr.implement.QrUsageManager;
import org.springframework.stereotype.Service;

/**
 * 회원의 QR 코드 발급을 담당하는 서비스.
 *
 * <p>새 QR을 발급할 때마다 이전 활성 QR을 자동으로 무효화한다.
 * 즉, 회원당 항상 하나의 유효한 QR만 존재한다.
 */
@RequiredArgsConstructor
@Service
public class QrService {

    private final QrTokenProvider qrTokenProvider;
    private final QrImageGenerator qrImageGenerator;
    private final QrUsageManager qrUsageManager;

    /**
     * 회원의 인증용 QR 코드 이미지를 생성한다.
     *
     * <ol>
     *   <li>이전 활성 QR이 존재하면 즉시 무효화한다.</li>
     *   <li>30초 만료 JWT 토큰을 생성한다.</li>
     *   <li>새 jti를 활성 QR로 등록한다.</li>
     *   <li>토큰을 QR 이미지(PNG)로 변환하여 반환한다.</li>
     * </ol>
     *
     * @param memberId QR을 발급받을 회원 ID
     * @return PNG 형식의 QR 이미지 바이트 배열
     */
    public byte[] generateMemberQr(Long memberId) {
        qrUsageManager.findActiveJti(memberId)
                .ifPresent(qrUsageManager::markAsUsed);

        CreatedQrToken created = qrTokenProvider.createToken(memberId);
        qrUsageManager.registerActiveJti(memberId, created.jti());

        return qrImageGenerator.generate(created.token());
    }

}