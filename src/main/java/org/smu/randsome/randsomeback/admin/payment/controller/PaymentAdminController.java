package org.smu.randsome.randsomeback.admin.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.payment.dto.request.PaymentRejectRequest;
import org.smu.randsome.randsomeback.admin.payment.dto.response.PaymentPreviewItem;
import org.smu.randsome.randsomeback.admin.payment.enums.PaymentFilterStatus;
import org.smu.randsome.randsomeback.admin.payment.service.PaymentAdminService;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PaymentAdminController extends PaymentAdminControllerDocs {

    private final PaymentAdminService paymentAdminService;

    @Override
    @PostMapping("/v1/admin/payments/{paymentId}/confirm")
    public ResponseEntity<ApiResponse<?>> confirm(@PathVariable Long paymentId) {
        paymentAdminService.confirm(paymentId);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Override
    @PostMapping("/v1/admin/payments/{paymentId}/reject")
    public ResponseEntity<ApiResponse<?>> reject(
            @PathVariable Long paymentId,
            @RequestBody @Valid PaymentRejectRequest request
    ) {
        paymentAdminService.reject(paymentId, request.rejectedReason());

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Override
    @GetMapping("/v1/admin/payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentPreviewItem>>> findPayments(
            @RequestParam PaymentFilterStatus filterStatus,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PaymentWithReason> payments = paymentAdminService.findPayments(filterStatus.toPaymentStatuses(), pageable);
        Page<PaymentPreviewItem> items = payments.map(PaymentPreviewItem::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
    }

}