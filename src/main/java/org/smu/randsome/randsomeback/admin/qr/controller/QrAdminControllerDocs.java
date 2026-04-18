package org.smu.randsome.randsomeback.admin.qr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.domain.qr.dto.request.QrVerifyRequest;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 QR 인증", description = "관리자 전용 QR 코드 인증 및 티켓 발급 API")
public abstract class QrAdminControllerDocs {

    @Operation(summary = "QR 코드 인증 및 티켓 발급", description = "관리자가 회원의 QR 코드를 인증하고 해당 회원에게 티켓을 발급합니다.")
    public abstract ApiResponse<?> verifyQrAndIssueTicket(@RequestBody QrVerifyRequest request);

}
