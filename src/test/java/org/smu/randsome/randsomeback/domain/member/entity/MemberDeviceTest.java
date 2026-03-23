package org.smu.randsome.randsomeback.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class MemberDeviceTest extends UnitTestSupport {

    private static final String DEVICE_TOKEN = "fcm_device_token_12345";

    @Test
    void register로_생성하면_member와_deviceToken과_lastSyncedAt이_설정된다() {
        // given
        Member member = MemberFixture.create();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 0, 0);

        // when
        MemberDevice memberDevice = MemberDevice.register(member, DEVICE_TOKEN, now);

        // then
        assertThat(memberDevice.getMember()).isEqualTo(member);
        assertThat(memberDevice.getDeviceToken()).isEqualTo(DEVICE_TOKEN);
        assertThat(memberDevice.getLastSyncedAt()).isEqualTo(now);
    }

    @Test
    void updateLastSyncedAt을_호출하면_lastSyncedAt이_갱신된다() {
        // given
        Member member = MemberFixture.create();
        LocalDateTime registeredAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        MemberDevice memberDevice = MemberDevice.register(member, DEVICE_TOKEN, registeredAt);

        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        memberDevice.updateLastSyncedAt(updatedAt);

        // then
        assertThat(memberDevice.getLastSyncedAt()).isEqualTo(updatedAt);
    }

}