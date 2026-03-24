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
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingAppliedEvent;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.implement.MemberDeviceReader;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

class NotificationHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    NotificationHandler notificationHandler;

    @Mock
    MemberDeviceReader memberDeviceReader;

    @Mock
    NotificationManager notificationManager;

    @Mock
    ErrorNotificationSender errorNotificationSender;

    @Test
    void 공지사항_이벤트_수신시_활성_디바이스에_알림을_전송한다() {
        // given
        var event = new AnnouncementRegisteredEvent(1L);
        var device = mock(MemberDevice.class);
        var member = mock(Member.class);
        given(device.getMember()).willReturn(member);
        given(member.getId()).willReturn(10L);
        given(device.getDeviceToken()).willReturn("fcm_token_1");
        given(memberDeviceReader.findAllActive()).willReturn(List.of(device));

        // when
        notificationHandler.announcementNotify(event);

        // then
        verify(notificationManager).sendToAll(List.of(10L), List.of("fcm_token_1"), NotificationType.ANNOUNCEMENT_REGISTERED);
    }

    @Test
    void 공지사항_이벤트_수신시_활성_디바이스가_없으면_알림을_전송하지_않는다() {
        // given
        var event = new AnnouncementRegisteredEvent(1L);
        given(memberDeviceReader.findAllActive()).willReturn(List.of());

        // when
        notificationHandler.announcementNotify(event);

        // then
        verify(notificationManager, never()).sendToAll(any(), any(), any());
    }

    @Test
    void 공지사항_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        var event = new AnnouncementRegisteredEvent(1L);
        willThrow(new RuntimeException("FCM 오류")).given(memberDeviceReader).findAllActive();

        // when & then
        assertThatNoException().isThrownBy(() -> notificationHandler.announcementNotify(event));
    }

    @Test
    void 매칭_신청_이벤트_수신시_어드민_디바이스에_알림을_전송한다() {
        // given
        var event = new MatchingAppliedEvent(5L);
        var device = mock(MemberDevice.class);
        var member = mock(Member.class);
        given(device.getMember()).willReturn(member);
        given(member.getId()).willReturn(20L);
        given(device.getDeviceToken()).willReturn("admin_token_1");
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of(device));

        // when
        notificationHandler.matchingApplicationNotify(event);

        // then
        verify(notificationManager).sendToAll(List.of(20L), List.of("admin_token_1"), NotificationType.MATCHING_APPLIED_TO_ADMIN);
    }

    @Test
    void 매칭_신청_이벤트_수신시_어드민_디바이스가_없으면_알림을_전송하지_않는다() {
        // given
        var event = new MatchingAppliedEvent(5L);
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of());

        // when
        notificationHandler.matchingApplicationNotify(event);

        // then
        verify(notificationManager, never()).sendToAll(any(), any(), any());
    }

    @Test
    void 매칭_신청_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        var event = new MatchingAppliedEvent(5L);
        willThrow(new RuntimeException("FCM 오류")).given(memberDeviceReader).findAllByAdminRole();

        // when & then
        assertThatNoException().isThrownBy(() -> notificationHandler.matchingApplicationNotify(event));
    }

    @Test
    void 후보자_신청_이벤트_수신시_어드민_디바이스에_알림을_전송한다() {
        // given
        var event = new CandidateAppliedEvent(7L);
        var device = mock(MemberDevice.class);
        var member = mock(Member.class);
        given(device.getMember()).willReturn(member);
        given(member.getId()).willReturn(20L);
        given(device.getDeviceToken()).willReturn("admin_token_1");
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of(device));

        // when
        notificationHandler.candidateRegistrationNotify(event);

        // then
        verify(notificationManager).sendToAll(List.of(20L), List.of("admin_token_1"), NotificationType.CANDIDATE_APPLIED_TO_ADMIN);
    }

    @Test
    void 후보자_신청_이벤트_수신시_어드민_디바이스가_없으면_알림을_전송하지_않는다() {
        // given
        var event = new CandidateAppliedEvent(7L);
        given(memberDeviceReader.findAllByAdminRole()).willReturn(List.of());

        // when
        notificationHandler.candidateRegistrationNotify(event);

        // then
        verify(notificationManager, never()).sendToAll(any(), any(), any());
    }

    @Test
    void 후보자_신청_알림_전송_중_예외_발생시_전파되지_않는다() {
        // given
        var event = new CandidateAppliedEvent(7L);
        willThrow(new RuntimeException("FCM 오류")).given(memberDeviceReader).findAllByAdminRole();

        // when & then
        assertThatNoException().isThrownBy(() -> notificationHandler.candidateRegistrationNotify(event));
    }

}
