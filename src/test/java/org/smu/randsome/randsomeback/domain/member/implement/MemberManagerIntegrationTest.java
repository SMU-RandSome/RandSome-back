package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRestrictionJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

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
    void 회원_정지_시_상태가_SUSPENDED로_변경되고_제한_기록이_DB에_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var reason = "규칙 위반";

        // when
        memberManager.suspend(member.getId(), reason);

        // then
        var updated = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.SUSPENDED);

        var restrictions = memberRestrictionJpaRepository.findAll();
        assertThat(restrictions).hasSize(1);
        assertThat(restrictions.get(0).getReason()).isEqualTo(reason);
    }

    @Test
    void 정지된_회원_복구_시_상태가_ACTIVE로_변경되어_DB에_반영된다() {
        // given
        var member = MemberFixture.create();
        member.suspend();
        var saved = memberJpaRepository.save(member);

        // when
        memberManager.restore(saved.getId());

        // then
        var updated = memberJpaRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

}
