package org.smu.randsome.randsomeback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.smu.randsome.randsomeback.domain.auth.controller.AuthController;
import org.smu.randsome.randsomeback.domain.auth.service.AuthService;
import org.smu.randsome.randsomeback.domain.candidate.controller.CandidateController;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.smu.randsome.randsomeback.domain.matching.controller.MatchingController;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.domain.member.controller.MemberController;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.domain.payment.controller.PaymentController;
import org.smu.randsome.randsomeback.domain.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ActiveProfiles("test")
@WebMvcTest(controllers = {
        AuthController.class,
        CandidateController.class,
        MatchingController.class,
        MemberController.class,
        PaymentController.class,
})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected CandidateService candidateService;

    @MockitoBean
    protected MatchingService matchingService;

    @MockitoBean
    protected MemberService memberService;

    @MockitoBean
    protected PaymentService paymentService;

}