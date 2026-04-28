package org.smu.randsome.randsomeback.domain.notification.implement;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateReader;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.implement.MemberDeviceReader;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

class NotificationAsyncExecutorUnitTest extends UnitTestSupport {

    @InjectMocks
    NotificationAsyncExecutor notificationAsyncExecutor;

    @Mock
    MemberDeviceReader memberDeviceReader;

    @Mock
    NotificationManager notificationManager;

    @Mock
    NotificationSender notificationSender;

    @Mock
    CandidateReader candidateReader;

    @Mock
    ErrorNotificationSender errorNotificationSender;

    @Test
    void 공지사항_알림_전송시_활성_디바이스에_알림을_전송한다() {
        // given
        var device = mock(MemberDevice.class);
        var member = mock(Member.class);
        given(device.getMember()).willReturn(member);
        given(member.getId()).willReturn(10L);
        given(device.getDeviceToken()).willReturn("fcm_token_1");
        given(memberDeviceReader.findAllActive()).willReturn(List.of(device));

        // when
        notificationAsyncExecutor.executeAnnouncementNotify(1L);

        // then
        verify(notificationManager).saveNotifications(List.of(10L), NotificationType.ANNOUNCEMENT_REGISTERED);
        verify(notificationSender).sendNotification(List.of("fcm_token_1"), NotificationType.ANNOUNCEMENT_REGISTERED);
    }

    @Test
    void 공지사항_알림_전송시_활성_디바이스가_없으면_알림을_전송하지_않는다() {
        // given
        given(memberDeviceReader.findAllActive()).willReturn(List.of());

        // when
        notificationAsyncExecutor.executeAnnouncementNotify(1L);

        // then
        verify(notificationManager, never()).saveNotifications(any(), any());
        verify(notificationSender, never()).sendNotification(any(), any());
    }

    @Test
    void 공지사항_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        willThrow(new RuntimeException("FCM 오류")).given(memberDeviceReader).findAllActive();

        // when & then
        assertThatNoException().isThrownBy(() -> notificationAsyncExecutor.executeAnnouncementNotify(1L));
    }

    @Test
    void 매칭_신청_알림_전송시_어드민_디바이스에_알림을_전송한다() {
        // given
        var device = mock(MemberDevice.class);
        var member = mock(Member.class);
        given(device.getMember()).willReturn(member);
        given(member.getId()).willReturn(20L);
        given(device.getDeviceToken()).willReturn("admin_token_1");
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of(device));

        // when
        notificationAsyncExecutor.executeMatchingApplicationNotify(5L);

        // then
        verify(notificationManager).saveNotifications(List.of(20L), NotificationType.MATCHING_APPLIED_TO_ADMIN);
        verify(notificationSender).sendNotification(List.of("admin_token_1"), NotificationType.MATCHING_APPLIED_TO_ADMIN);
    }

    @Test
    void 매칭_신청_알림_전송시_어드민_디바이스가_없으면_알림을_전송하지_않는다() {
        // given
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of());

        // when
        notificationAsyncExecutor.executeMatchingApplicationNotify(5L);

        // then
        verify(notificationManager, never()).saveNotifications(any(), any());
        verify(notificationSender, never()).sendNotification(any(), any());
    }

    @Test
    void 매칭_신청_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        willThrow(new RuntimeException("FCM 오류")).given(memberDeviceReader).findAllByAdminRole();

        // when & then
        assertThatNoException().isThrownBy(() -> notificationAsyncExecutor.executeMatchingApplicationNotify(5L));
    }

    @Test
    void 후보자_신청_알림_전송시_어드민_디바이스에_알림을_전송한다() {
        // given
        var device = mock(MemberDevice.class);
        var member = mock(Member.class);
        given(device.getMember()).willReturn(member);
        given(member.getId()).willReturn(20L);
        given(device.getDeviceToken()).willReturn("admin_token_1");
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of(device));

        // when
        notificationAsyncExecutor.executeCandidateRegistrationNotify(7L);

        // then
        verify(notificationManager).saveNotifications(List.of(20L), NotificationType.CANDIDATE_APPLIED_TO_ADMIN);
        verify(notificationSender).sendNotification(List.of("admin_token_1"), NotificationType.CANDIDATE_APPLIED_TO_ADMIN);
    }

    @Test
    void 후보자_신청_알림_전송시_어드민_디바이스가_없으면_알림을_전송하지_않는다() {
        // given
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of());

        // when
        notificationAsyncExecutor.executeCandidateRegistrationNotify(7L);

        // then
        verify(notificationManager, never()).saveNotifications(any(), any());
        verify(notificationSender, never()).sendNotification(any(), any());
    }

    @Test
    void 후보자_신청_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        willThrow(new RuntimeException("FCM 오류")).given(memberDeviceReader).findAllByAdminRole();

        // when & then
        assertThatNoException().isThrownBy(() -> notificationAsyncExecutor.executeCandidateRegistrationNotify(7L));
    }

    @Test
    void 후보자_승인_알림_전송시_해당_회원_디바이스에_알림을_전송한다() {
        // given
        var candidateRegistration = mock(CandidateRegistration.class);
        var member = mock(Member.class);
        var device = mock(MemberDevice.class);
        given(candidateReader.findWithMember(3L)).willReturn(candidateRegistration);
        given(candidateRegistration.getMember()).willReturn(member);
        given(member.getId()).willReturn(30L);
        given(memberDeviceReader.findAllByMemberId(30L)).willReturn(List.of(device));
        given(device.getMember()).willReturn(member);
        given(device.getDeviceToken()).willReturn("user_token_1");

        // when
        notificationAsyncExecutor.executeCandidateRegistrationResultNotify(3L, NotificationType.CANDIDATE_APPROVED);

        // then
        verify(notificationManager).saveNotifications(List.of(30L), NotificationType.CANDIDATE_APPROVED);
        verify(notificationSender).sendNotification(List.of("user_token_1"), NotificationType.CANDIDATE_APPROVED);
    }

    @Test
    void 후보자_승인_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        willThrow(new RuntimeException("FCM 오류")).given(candidateReader).findWithMember(3L);

        // when & then
        assertThatNoException().isThrownBy(
                () -> notificationAsyncExecutor.executeCandidateRegistrationResultNotify(3L, NotificationType.CANDIDATE_APPROVED));
    }

}
