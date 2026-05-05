package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberDeviceJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class MemberDeviceReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberDeviceReader memberDeviceReader;

    @Mock
    MemberDeviceJpaRepository memberDeviceJpaRepository;

    @Test
    void 활성화된_모든_디바이스를_조회한다() {
        // given
        var member = MemberFixture.create();
        var device = MemberDevice.register(member, "fcm_token", LocalDateTime.now());
        given(memberDeviceJpaRepository.findAllByStatus(EntityStatus.ACTIVE))
                .willReturn(List.of(device));

        // when
        var result = memberDeviceReader.findAllActive();

        // then
        assertThat(result).hasSize(1);
        verify(memberDeviceJpaRepository).findAllByStatus(EntityStatus.ACTIVE);
    }

    @Test
    void 활성화된_디바이스가_없으면_빈_목록을_반환한다() {
        // given
        given(memberDeviceJpaRepository.findAllByStatus(EntityStatus.ACTIVE))
                .willReturn(List.of());

        // when
        var result = memberDeviceReader.findAllActive();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 어드민_회원의_디바이스를_조회한다() {
        // given
        var admin = MemberFixture.create();
        admin.updateRole(Role.ROLE_ADMIN);
        var device = MemberDevice.register(admin, "admin_fcm_token", LocalDateTime.now());
        given(memberDeviceJpaRepository.findAllByAdmin(Role.ROLE_ADMIN, EntityStatus.ACTIVE))
                .willReturn(List.of(device));

        // when
        var result = memberDeviceReader.findAllByAdminRole();

        // then
        assertThat(result).hasSize(1);
        verify(memberDeviceJpaRepository).findAllByAdmin(Role.ROLE_ADMIN, EntityStatus.ACTIVE);
    }

    @Test
    void 어드민_디바이스가_없으면_빈_목록을_반환한다() {
        // given
        given(memberDeviceJpaRepository.findAllByAdmin(Role.ROLE_ADMIN, EntityStatus.ACTIVE))
                .willReturn(List.of());

        // when
        var result = memberDeviceReader.findAllByAdminRole();

        // then
        assertThat(result).isEmpty();
    }

}