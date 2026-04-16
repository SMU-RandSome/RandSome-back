package org.smu.randsome.randsomeback.admin.qr.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.qr.service.QrAdminService;
import org.smu.randsome.randsomeback.domain.qr.dto.request.QrVerifyRequest;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class QrAdminController extends QrAdminControllerDocs {

    private final QrAdminService qrAdminService;

    @Override
    @PostMapping("/v1/admin/qr/verify")
    public ApiResponse<?> verifyQrAndIssueTicket(@RequestBody @Valid QrVerifyRequest request) {
        qrAdminService.verifyQrAndIssueTicket(request.qrToken(), request.ticketType());

        return ApiResponse.success();
    }

}
