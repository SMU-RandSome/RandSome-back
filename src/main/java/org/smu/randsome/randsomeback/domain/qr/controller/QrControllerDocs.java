package org.smu.randsome.randsomeback.domain.qr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;

@Tag(name = "QR Docs", description = "QR 코드 관련 API 문서")
public abstract class QrControllerDocs {

    @Operation(summary = "QR 코드 발급 - JWT [O]",
            description = """
                    회원 인증용 QR 코드 이미지(PNG)를 발급한다.
                    - QR 코드 내부에 서명된 단기 토큰(30초 만료)이 포함된다.
                    - 일회용으로, 스캔 후 사용 처리되면 재사용이 불가하다.
                    - 응답 Content-Type: image/png
                    """
    )
    @ApiExceptions(values = {
            ErrorType.QR_GENERATION_FAILED,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<byte[]> generateMemberQr(@LoginMember Long memberId);

}
