package org.smu.randsome.randsomeback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.smu.randsome.randsomeback.admin.announcement.controller.AnnouncementAdminController;
import org.smu.randsome.randsomeback.admin.announcement.service.AnnouncementAdminService;
import org.smu.randsome.randsomeback.admin.candidate.controller.CandidateAdminController;
import org.smu.randsome.randsomeback.admin.candidate.service.CandidateAdminService;
import org.smu.randsome.randsomeback.admin.coupon.controller.CouponEventAdminController;
import org.smu.randsome.randsomeback.admin.coupon.service.CouponEventAdminService;
import org.smu.randsome.randsomeback.admin.matching.controller.AdminMatchingController;
import org.smu.randsome.randsomeback.admin.matching.service.AdminMatchingService;
import org.smu.randsome.randsomeback.admin.member.controller.MemberAdminController;
import org.smu.randsome.randsomeback.admin.member.service.MemberAdminService;
import org.smu.randsome.randsomeback.admin.qr.service.QrAdminService;
import org.smu.randsome.randsomeback.admin.report.controller.AdminReportController;
import org.smu.randsome.randsomeback.admin.report.service.AdminReportService;
import org.smu.randsome.randsomeback.admin.statistics.controller.StatisticsAdminController;
import org.smu.randsome.randsomeback.admin.statistics.service.StatisticsAdminService;
import org.smu.randsome.randsomeback.domain.announcement.controller.AnnouncementController;
import org.smu.randsome.randsomeback.domain.announcement.service.AnnouncementService;
import org.smu.randsome.randsomeback.domain.attendance.controller.AttendanceController;
import org.smu.randsome.randsomeback.domain.attendance.service.AttendanceService;
import org.smu.randsome.randsomeback.domain.auth.controller.AuthController;
import org.smu.randsome.randsomeback.domain.auth.service.AuthService;
import org.smu.randsome.randsomeback.domain.auth.service.EmailVerificationService;
import org.smu.randsome.randsomeback.domain.candidate.controller.CandidateController;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.smu.randsome.randsomeback.domain.coupon.controller.CouponController;
import org.smu.randsome.randsomeback.domain.coupon.controller.CouponEventController;
import org.smu.randsome.randsomeback.domain.coupon.service.CouponEventService;
import org.smu.randsome.randsomeback.domain.coupon.service.CouponService;
import org.smu.randsome.randsomeback.domain.feed.FeedController;
import org.smu.randsome.randsomeback.domain.feed.FeedService;
import org.smu.randsome.randsomeback.domain.matching.controller.MatchingController;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.domain.member.controller.MemberController;
import org.smu.randsome.randsomeback.domain.member.service.MemberDeviceService;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.domain.qr.controller.QrController;
import org.smu.randsome.randsomeback.domain.qr.service.QrService;
import org.smu.randsome.randsomeback.domain.report.controller.ReportController;
import org.smu.randsome.randsomeback.domain.report.service.ReportService;
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
        AnnouncementController.class,
        AuthController.class,
        CandidateController.class,
        FeedController.class,
        MatchingController.class,
        MemberController.class,
        StatisticsController.class,
        TicketController.class,
        AttendanceController.class,
        QrController.class,
        CouponEventController.class,
        CouponController.class,
        ReportController.class,

        CouponEventAdminController.class,
        MemberAdminController.class,
        StatisticsAdminController.class,
        AnnouncementAdminController.class,
        AdminReportController.class,
        CandidateAdminController.class,
        AdminMatchingController.class,

        org.smu.randsome.randsomeback.admin.qr.controller.QrAdminController.class
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
    protected MemberAdminService memberAdminService;

    @MockitoBean
    protected MemberDeviceService memberDeviceService;


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

    @MockitoBean
    protected AttendanceService attendanceService;

    @MockitoBean
    protected QrService qrService;

    @MockitoBean
    protected QrAdminService qrAdminService;

    @MockitoBean
    protected CouponEventAdminService couponEventAdminService;

    @MockitoBean
    protected CouponEventService couponEventService;

    @MockitoBean
    protected CouponService couponService;

    @MockitoBean
    protected ReportService reportService;

    @MockitoBean
    protected AdminReportService adminReportService;

    @MockitoBean
    protected CandidateAdminService candidateAdminService;

    @MockitoBean
    protected AdminMatchingService adminMatchingService;

}