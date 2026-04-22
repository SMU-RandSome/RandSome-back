package org.smu.randsome.randsomeback.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.SuspensionManager;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRestrictionJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.PageRequest;

@RequiredArgsConstructor
class MemberAdminServiceIntegrationTest extends IntegrationTestSupport {

    final MemberAdminService memberAdminService;
    final MemberJpaRepository memberJpaRepository;
    final MemberRestrictionJpaRepository memberRestrictionJpaRepository;
    final SuspensionManager suspensionManager;

    @AfterEach
    void tearDown() {
        memberRestrictionJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 회원_목록_페이징_조회_시_저장된_회원이_반환된다() {
        // given
        memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", org.smu.randsome.randsomeback.domain.member.enums.Gender.MALE));
        memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", org.smu.randsome.randsomeback.domain.member.enums.Gender.FEMALE));
        var pageable = PageRequest.of(0, 10);

        // when
        var result = memberAdminService.getMembers(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 회원_상세_조회_시_회원_정보를_올바르게_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        // when
        var result = memberAdminService.getMemberDetail(member.getId());

        // then
        assertThat(result.id()).isEqualTo(member.getId());
        assertThat(result.legalName()).isEqualTo(MemberFixture.DEFAULT_LEGAL_NAME);
        assertThat(result.email()).isEqualTo(MemberFixture.DEFAULT_EMAIL);
    }

    @Test
    void 회원_정지_시_해당_회원_상태가_SUSPENDED로_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        // when
        memberAdminService.suspendMember(member.getId(), "부적절한 행동");

        // then
        var updated = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.SUSPENDED);
        assertThat(updated.getRole()).isEqualTo(Role.ROLE_SUSPEND_MEMBER);
        assertThat(updated.getRefreshToken()).isNull();
        assertThat(suspensionManager.isSuspended(member.getId())).isTrue();
    }

    @Test
    void 회원_복구_시_해당_회원_상태가_ACTIVE로_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        memberAdminService.suspendMember(member.getId(), "부적절한 행동");

        // when
        memberAdminService.restoreMember(member.getId());

        // then
        var updated = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(updated.getRole()).isEqualTo(Role.ROLE_MEMBER);
        assertThat(suspensionManager.isSuspended(member.getId())).isFalse();
    }

}
