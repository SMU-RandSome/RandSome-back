package org.smu.randsome.randsomeback.domain.qr.implement;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

/**
 * ZXing 라이브러리를 사용하여 문자열을 QR 코드 PNG 이미지로 변환하는 컴포넌트.
 *
 * <p>이미지 스펙:
 * <ul>
 *   <li>크기: 300 × 300 픽셀</li>
 *   <li>포맷: PNG</li>
 *   <li>오류 정정 레벨: M (최대 15% 손상 복구)</li>
 * </ul>
 */
@Component
public class QrImageGenerator {

    private static final int SIZE = 300;
    private static final String FORMAT = "PNG";

    /**
     * 주어진 문자열 내용을 QR 코드 PNG 이미지로 변환한다.
     *
     * @param content QR 코드에 인코딩할 문자열 (JWT 토큰)
     * @return PNG 이미지 바이트 배열
     * @throws CoreException {@link ErrorType#QR_GENERATION_FAILED} - ZXing 인코딩 또는 I/O 오류 발생 시
     */
    public byte[] generate(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    SIZE,
                    SIZE,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, FORMAT, outputStream);

            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new CoreException(ErrorType.QR_GENERATION_FAILED);
        }
    }

}