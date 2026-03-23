package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.repository.MemberDeviceJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

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
        Member member = MemberFixture.create();
        LocalDateTime registeredAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        MemberDevice existingDevice = MemberDevice.register(member, DEVICE_TOKEN, registeredAt);

        given(memberDeviceJpaRepository.findByMemberIdAndDeviceTokenAndStatus(1L, DEVICE_TOKEN, EntityStatus.ACTIVE))
                .willReturn(Optional.of(existingDevice));

        LocalDateTime syncTime = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        memberDeviceManager.syncDeviceToken(1L, DEVICE_TOKEN, syncTime);

        // then
        assertThat(existingDevice.getLastSyncedAt()).isEqualTo(syncTime);
        verify(memberDeviceJpaRepository, never()).save(any());
    }

    @Test
    void ACTIVE_토큰이_없으면_새_디바이스_토큰을_등록한다() {
        // given
        Member member = MemberFixture.create();
        given(memberDeviceJpaRepository.findByMemberIdAndDeviceTokenAndStatus(1L, DEVICE_TOKEN, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(memberReader.find(1L)).willReturn(member);

        LocalDateTime now = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        memberDeviceManager.syncDeviceToken(1L, DEVICE_TOKEN, now);

        // then
        ArgumentCaptor<MemberDevice> captor = ArgumentCaptor.forClass(MemberDevice.class);
        verify(memberDeviceJpaRepository).save(captor.capture());

        MemberDevice saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getDeviceToken()).isEqualTo(DEVICE_TOKEN);
        assertThat(saved.getLastSyncedAt()).isEqualTo(now);
    }

}