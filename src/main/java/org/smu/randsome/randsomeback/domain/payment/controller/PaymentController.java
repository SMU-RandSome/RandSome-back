package org.smu.randsome.randsomeback.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.payment.service.PaymentService;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PaymentController extends PaymentControllerDocs {

    private final PaymentService paymentService;

}