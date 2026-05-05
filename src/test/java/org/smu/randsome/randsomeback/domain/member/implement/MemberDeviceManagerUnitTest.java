package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.repository.MemberDeviceJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.dao.DataIntegrityViolationException;

class MemberDeviceManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberDeviceManager memberDeviceManager;

    @Mock
    MemberReader memberReader;

    @Mock
    MemberDeviceJpaRepository memberDeviceJpaRepository;

    private static final String DEVICE_TOKEN = "fcm_device_token_12345";

    @Test
    void ACTIVE_토큰이_이미_존재하면_lastSyncedAt을_업데이트한다() {
        // given
        var member = MemberFixture.create();
        var registeredAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        var existingDevice = MemberDevice.register(member, DEVICE_TOKEN, registeredAt);

        given(memberDeviceJpaRepository.findByMemberIdAndDeviceToken(1L, DEVICE_TOKEN))
                .willReturn(Optional.of(existingDevice));

        var syncTime = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        memberDeviceManager.syncDeviceToken(1L, DEVICE_TOKEN, syncTime);

        // then
        assertThat(existingDevice.getLastSyncedAt()).isEqualTo(syncTime);
        assertThat(existingDevice.isActive()).isTrue();
        verify(memberDeviceJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    void DELETED_토큰이_존재하면_재활성화한다() {
        // given
        var member = MemberFixture.create();
        var deletedDevice = MemberDevice.register(member, DEVICE_TOKEN, LocalDateTime.of(2024, 1, 1, 0, 0));
        deletedDevice.delete();

        given(memberDeviceJpaRepository.findByMemberIdAndDeviceToken(1L, DEVICE_TOKEN))
                .willReturn(Optional.of(deletedDevice));

        var syncTime = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        memberDeviceManager.syncDeviceToken(1L, DEVICE_TOKEN, syncTime);

        // then
        assertThat(deletedDevice.isActive()).isTrue();
        assertThat(deletedDevice.getLastSyncedAt()).isEqualTo(syncTime);
        verify(memberDeviceJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    void 토큰이_없으면_새_디바이스_토큰을_등록한다() {
        // given
        var member = MemberFixture.create();
        given(memberDeviceJpaRepository.findByMemberIdAndDeviceToken(1L, DEVICE_TOKEN))
                .willReturn(Optional.empty());
        given(memberReader.find(1L)).willReturn(member);

        var now = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        memberDeviceManager.syncDeviceToken(1L, DEVICE_TOKEN, now);

        // then
        ArgumentCaptor<MemberDevice> captor = ArgumentCaptor.forClass(MemberDevice.class);
        verify(memberDeviceJpaRepository).saveAndFlush(captor.capture());

        MemberDevice saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getDeviceToken()).isEqualTo(DEVICE_TOKEN);
        assertThat(saved.getLastSyncedAt()).isEqualTo(now);
    }

    @Test
    void 동시_요청으로_중복_등록_시_예외_없이_무시한다() {
        // given
        var member = MemberFixture.create();
        given(memberDeviceJpaRepository.findByMemberIdAndDeviceToken(1L, DEVICE_TOKEN))
                .willReturn(Optional.empty());
        given(memberReader.find(1L)).willReturn(member);
        given(memberDeviceJpaRepository.saveAndFlush(any(MemberDevice.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        var now = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when & then — 예외가 전파되지 않아야 한다
        memberDeviceManager.syncDeviceToken(1L, DEVICE_TOKEN, now);
    }

    @Test
    void 디바이스_토큰을_soft_delete_한다() {
        // given
        var member = MemberFixture.create();
        var memberDevice = MemberDevice.register(member, DEVICE_TOKEN, LocalDateTime.now());
        given(memberDeviceJpaRepository.findByMemberIdAndDeviceTokenAndStatus(1L, DEVICE_TOKEN, EntityStatus.ACTIVE))
                .willReturn(Optional.of(memberDevice));

        // when
        memberDeviceManager.deleteDeviceToken(1L, DEVICE_TOKEN);

        // then
        assertThat(memberDevice.isDeleted()).isTrue();
    }

    @Test
    void 회원의_활성화된_토큰이_없으면_예외를_반환한다() {
        // given
        given(memberDeviceJpaRepository.findByMemberIdAndDeviceTokenAndStatus(1L, DEVICE_TOKEN, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> memberDeviceManager.deleteDeviceToken(1L, DEVICE_TOKEN))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_FCM_TOKEN.getMessage());
    }

}