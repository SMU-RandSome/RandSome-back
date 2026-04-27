package org.smu.randsome.randsomeback.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.SuspensionManager;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRestrictionJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSearchCondition;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;

@RequiredArgsConstructor
class MemberAdminServiceIntegrationTest extends IntegrationTestSupport {

    final MemberAdminService memberAdminService;
    final MemberJpaRepository memberJpaRepository;
    final MemberProfileTagJpaRepository memberProfileTagJpaRepository;
    final MemberRestrictionJpaRepository memberRestrictionJpaRepository;
    final SuspensionManager suspensionManager;

    @AfterEach
    void tearDown() {
        memberRestrictionJpaRepository.deleteAll();
        memberProfileTagJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 회원_목록_페이징_조회_시_저장된_회원이_반환된다() {
        // given
        memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", Gender.MALE));
        memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", Gender.FEMALE));

        // when
        var result = memberAdminService.findMembers(new MemberSearchCondition(null), new OffsetLimit(1, 10));

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void 회원_목록_조회_시_정지된_회원도_포함된다() {
        // given
        var active = memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", Gender.MALE));
        var suspended = memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", Gender.FEMALE));
        memberAdminService.suspendMember(suspended.getId(), "테스트 정지");

        // when
        var result = memberAdminService.findMembers(new MemberSearchCondition(null), new OffsetLimit(1, 10));

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).extracting(r -> r.getId())
                .containsExactlyInAnyOrder(active.getId(), suspended.getId());
    }

    @Test
    void 회원_목록_조회_시_삭제된_회원은_제외된다() {
        // given
        var active = memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", Gender.MALE));
        var deleted = memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", Gender.FEMALE));
        deleted.delete();
        memberJpaRepository.save(deleted);

        // when
        var result = memberAdminService.findMembers(new MemberSearchCondition(null), new OffsetLimit(1, 10));

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().getId()).isEqualTo(active.getId());
    }

    @Test
    void 닉네임으로_회원을_검색한다() {
        // given
        var target = memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", Gender.MALE));
        memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", Gender.FEMALE));
        String nickname = target.getNickname();

        // when
        var result = memberAdminService.findMembers(new MemberSearchCondition(nickname), new OffsetLimit(1, 10));

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    void 실명으로_회원을_검색한다() {
        // given
        var target = memberJpaRepository.save(
                MemberFixture.createWithLegalName("202300001@sangmyung.kr", "김검색"));
        memberJpaRepository.save(
                MemberFixture.createWithLegalName("202300002@sangmyung.kr", "이다름"));

        // when
        var result = memberAdminService.findMembers(new MemberSearchCondition("김검색"), new OffsetLimit(1, 10));

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    void 검색어가_없으면_전체_회원을_조회한다() {
        // given
        memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", Gender.MALE));
        memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", Gender.FEMALE));

        // when
        var result = memberAdminService.findMembers(new MemberSearchCondition(null), new OffsetLimit(1, 10));

        // then
        assertThat(result.content()).hasSize(2);
    }

    @Test
    void 회원_상세_조회_시_회원_정보를_올바르게_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        // when
        var result = memberAdminService.findMemberDetail(member.getId());

        // then
        assertThat(result.id()).isEqualTo(member.getId());
        assertThat(result.legalName()).isEqualTo(MemberFixture.DEFAULT_LEGAL_NAME);
        assertThat(result.email()).isEqualTo(MemberFixture.DEFAULT_EMAIL);
    }

    @Test
    void 정지된_회원도_상세_조회할_수_있다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        memberAdminService.suspendMember(member.getId(), "테스트 정지");

        // when
        var result = memberAdminService.findMemberDetail(member.getId());

        // then
        assertThat(result.id()).isEqualTo(member.getId());
    }

    @Test
    void 삭제된_회원_상세_조회_시_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        member.delete();
        memberJpaRepository.save(member);

        // when & then
        assertThatThrownBy(() -> memberAdminService.findMemberDetail(member.getId()))
                .isInstanceOf(CoreException.class);
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
