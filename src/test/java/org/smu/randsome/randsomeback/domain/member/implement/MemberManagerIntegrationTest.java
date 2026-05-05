package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRestrictionJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@RequiredArgsConstructor
class MemberManagerIntegrationTest extends IntegrationTestSupport {

    final MemberManager memberManager;
    final MemberJpaRepository memberJpaRepository;
    final MemberRestrictionJpaRepository memberRestrictionJpaRepository;

    @AfterEach
    void tearDown() {
        memberRestrictionJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 회원_정지_시_status_role_refreshToken이_변경되고_제한_기록이_DB에_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var reason = "규칙 위반";

        // when
        memberManager.suspend(member.getId(), reason);

        // then
        var updated = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.SUSPENDED);
        assertThat(updated.getRole()).isEqualTo(Role.ROLE_SUSPEND_MEMBER);
        assertThat(updated.getRefreshToken()).isNull();

        var restrictions = memberRestrictionJpaRepository.findAll();
        assertThat(restrictions).hasSize(1);
        assertThat(restrictions.get(0).getReason()).isEqualTo(reason);
    }

    @Test
    void 이미_정지된_회원을_재정지하면_예외가_발생한다() {
        // given
        var member = MemberFixture.create();
        member.suspend();
        var saved = memberJpaRepository.save(member);

        // when // then
        assertThatThrownBy(() -> memberManager.suspend(saved.getId(), "재정지 시도"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 정지된_회원_복구_시_status와_role이_ACTIVE와_ROLE_MEMBER로_변경된다() {
        // given
        var member = MemberFixture.create();
        member.suspend(); // status=SUSPENDED, role=ROLE_SUSPEND_MEMBER
        var saved = memberJpaRepository.save(member);

        // when
        memberManager.restore(saved.getId());

        // then
        var updated = memberJpaRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(updated.getRole()).isEqualTo(Role.ROLE_MEMBER);
    }

    @Test
    void 정지_후_복구_전체_사이클이_정상_동작한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var reason = "규칙 위반";

        // when - 정지
        memberManager.suspend(member.getId(), reason);

        var suspended = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(suspended.getStatus()).isEqualTo(EntityStatus.SUSPENDED);
        assertThat(suspended.getRole()).isEqualTo(Role.ROLE_SUSPEND_MEMBER);

        // when - 복구
        memberManager.restore(member.getId());

        // then
        var restored = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(restored.getRole()).isEqualTo(Role.ROLE_MEMBER);

        // 제한 기록은 복구 후에도 유지
        var restrictions = memberRestrictionJpaRepository.findAll();
        assertThat(restrictions).hasSize(1);
        assertThat(restrictions.get(0).getReason()).isEqualTo(reason);
    }

}
