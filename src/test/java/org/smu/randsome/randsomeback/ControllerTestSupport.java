package org.smu.randsome.randsomeback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.smu.randsome.randsomeback.admin.announcement.controller.AnnouncementAdminController;
import org.smu.randsome.randsomeback.admin.announcement.service.AnnouncementAdminService;
import org.smu.randsome.randsomeback.admin.member.controller.MemberAdminController;
import org.smu.randsome.randsomeback.admin.member.service.MemberAdminService;
import org.smu.randsome.randsomeback.admin.payment.controller.PaymentAdminController;
import org.smu.randsome.randsomeback.admin.payment.service.PaymentAdminService;
import org.smu.randsome.randsomeback.admin.statistics.controller.StatisticsAdminController;
import org.smu.randsome.randsomeback.admin.statistics.service.StatisticsAdminService;
import org.smu.randsome.randsomeback.domain.announcement.controller.AnnouncementController;
import org.smu.randsome.randsomeback.domain.announcement.service.AnnouncementService;
import org.smu.randsome.randsomeback.domain.auth.controller.AuthController;
import org.smu.randsome.randsomeback.domain.auth.service.AuthService;
import org.smu.randsome.randsomeback.domain.auth.service.EmailVerificationService;
import org.smu.randsome.randsomeback.domain.bankaccount.service.BankAccountService;
import org.smu.randsome.randsomeback.domain.candidate.controller.CandidateController;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.smu.randsome.randsomeback.domain.feed.FeedController;
import org.smu.randsome.randsomeback.domain.feed.FeedService;
import org.smu.randsome.randsomeback.domain.matching.controller.MatchingController;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.domain.member.controller.MemberController;
import org.smu.randsome.randsomeback.domain.member.service.MemberDeviceService;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.domain.payment.controller.PaymentController;
import org.smu.randsome.randsomeback.domain.payment.service.PaymentService;
import org.smu.randsome.randsomeback.domain.statistics.controller.StatisticsController;
import org.smu.randsome.randsomeback.domain.statistics.service.StatisticsService;
import org.smu.randsome.randsomeback.domain.ticket.controller.TicketController;
import org.smu.randsome.randsomeback.domain.ticket.service.TicketService;
import org.smu.randsome.randsomeback.security.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@ActiveProfiles("test")
@Import({TestSecurityConfig.class, MethodValidationPostProcessor.class})
@WebMvcTest(controllers = {
        AnnouncementAdminController.class,
        AnnouncementController.class,
        AuthController.class,
        CandidateController.class,
        FeedController.class,
        MatchingController.class,
        MemberController.class,
        MemberAdminController.class,
        PaymentController.class,
        PaymentAdminController.class,
        StatisticsAdminController.class,
        StatisticsController.class,
        TicketController.class,
})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected FeedService feedService;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected EmailVerificationService emailVerificationService;

    @MockitoBean
    protected CandidateService candidateService;

    @MockitoBean
    protected MatchingService matchingService;

    @MockitoBean
    protected MemberService memberService;

    @MockitoBean
    protected BankAccountService bankAccountService;

    @MockitoBean
    protected MemberAdminService memberAdminService;

    @MockitoBean
    protected MemberDeviceService memberDeviceService;

    @MockitoBean
    protected PaymentService paymentService;

    @MockitoBean
    protected PaymentAdminService paymentAdminService;

    @MockitoBean
    protected StatisticsAdminService statisticsAdminService;

    @MockitoBean
    protected StatisticsService statisticsService;

    @MockitoBean
    protected AnnouncementAdminService announcementAdminService;

    @MockitoBean
    protected AnnouncementService announcementService;

    @MockitoBean
    protected TicketService ticketService;

}