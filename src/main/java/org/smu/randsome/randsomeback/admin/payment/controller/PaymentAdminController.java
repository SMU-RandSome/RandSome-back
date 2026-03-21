package org.smu.randsome.randsomeback.admin.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.payment.dto.request.PaymentRejectRequest;
import org.smu.randsome.randsomeback.admin.payment.dto.response.PaymentPreviewItem;
import org.smu.randsome.randsomeback.admin.payment.enums.PaymentFilterStatus;
import org.smu.randsome.randsomeback.admin.payment.service.PaymentAdminService;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearch;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<?> confirm(@PathVariable Long paymentId) {
        paymentAdminService.confirm(paymentId);

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/admin/payments/{paymentId}/reject")
    public ApiResponse<?> reject(
            @PathVariable Long paymentId,
            @RequestBody @Valid PaymentRejectRequest request
    ) {
        paymentAdminService.reject(paymentId, request.rejectedReason());

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/admin/payments")
    public ApiResponse<PageResponse<PaymentPreviewItem>> findPayments(
            @RequestParam PaymentFilterStatus filterStatus,
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PaymentWithReason> payments = paymentAdminService.findPayments(
                new PaymentSearch(filterStatus.toPaymentStatuses(), query),
                pageable
        );
        Page<PaymentPreviewItem> items = payments.map(PaymentPreviewItem::from);

        return ApiResponse.success(PageResponse.from(items));
    }

}