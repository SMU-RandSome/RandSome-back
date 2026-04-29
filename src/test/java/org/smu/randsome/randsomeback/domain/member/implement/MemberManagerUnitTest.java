package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberRestriction;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRestrictionJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberManager memberManager;

    @Mock
    MemberJpaRepository memberJpaRepository;

    @Mock
    MemberRestrictionJpaRepository memberRestrictionJpaRepository;

    @Mock
    MemberProfileTagManager memberProfileTagManager;

    @Mock
    MemberDeviceManager memberDeviceManager;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    SuspensionManager suspensionManager;

    @Test
    void 회원을_생성하고_프로필_태그가_함께_생성된다() {
        // given
        given(memberJpaRepository.existsByEmail_AddressAndStatus(any(String.class), any(EntityStatus.class)))
                .willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded-password");
        given(memberJpaRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member member = memberManager.create(
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                MemberFixture.createTagsInfo()
        );

        // then
        assertThat(member).isNotNull();
        assertThat(member.getRole()).isEqualTo(Role.ROLE_MEMBER);
        verify(memberProfileTagManager).create(
                any(Member.class),
                any(),
                any(),
                any()
        );
    }

    @Test
    void 이미_존재하는_이메일이면_예외가_발생한다() {
        // given
        given(memberJpaRepository.existsByEmail_AddressAndStatus(any(String.class), any(EntityStatus.class)))
                .willReturn(true);

        // when // then
        assertThatThrownBy(() -> memberManager.create(
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                MemberFixture.createTagsInfo()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void TOCTOU_경쟁_조건으로_save에서_DataIntegrityViolationException_발생_시_DUPLICATE_EMAIL_예외가_발생한다() {
        // given
        given(memberJpaRepository.existsByEmail_AddressAndStatus(any(String.class), any(EntityStatus.class)))
                .willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded-password");
        willThrow(DataIntegrityViolationException.class)
                .given(memberJpaRepository).save(any(Member.class));

        // when // then
        assertThatThrownBy(() -> memberManager.create(
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                MemberFixture.createTagsInfo()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 프로필을_업데이트한다() {
        // given
        Member member = MemberFixture.create();
        given(memberJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE)).willReturn(Optional.of(member));

        var updateProfile = UpdateProfile.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .department(Department.ELECTRONICS_ENGINEERING)
                .instagramId("new_insta")
                .selfIntroduction("새 자기소개")
                .idealDescription("새 이상형")
                .personalityTag(MemberFixture.DEFAULT_PERSONALITY_TAG)
                .faceTypeTag(MemberFixture.DEFAULT_FACE_TYPE_TAG)
                .datingStyleTag(MemberFixture.DEFAULT_DATING_STYLE_TAG)
                .build();

        // when
        memberManager.updateProfile(1L, updateProfile);

        // then
        assertThat(member.getLegalName()).isEqualTo("김철수");
        assertThat(member.getMbti()).isEqualTo(Mbti.ENFP);
        assertThat(member.getSocialProfile()).extracting(
                SocialProfile::instagramId,
                SocialProfile::selfIntroduction,
                SocialProfile::idealDescription
        ).containsExactly(
                "new_insta",
                "새 자기소개",
                "새 이상형"
        );
    }

    @Test
    void 프로필_업데이트_시_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        given(memberJpaRepository.findByIdAndStatus(999L, EntityStatus.ACTIVE)).willReturn(Optional.empty());

        var updateProfile = UpdateProfile.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .build();

        // when // then
        assertThatThrownBy(() -> memberManager.updateProfile(999L, updateProfile))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 로그아웃_시_refreshToken이_null로_변경된다() {
        // given
        Member member = MemberFixture.create();
        member.updateRefreshToken("existing.refresh.token");
        given(memberJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE)).willReturn(Optional.of(member));

        // when
        memberManager.logout(1L);

        // then
        assertThat(member.getRefreshToken()).isNull();
    }

    @Test
    void 로그아웃_시_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        given(memberJpaRepository.findByIdAndStatus(999L, EntityStatus.ACTIVE)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> memberManager.logout(999L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 비밀번호를_변경한다() {
        // given
        var member = MemberFixture.create();
        String newPassword = "newPassword123!";
        String encodedNewPassword = "encoded-new-password";
        given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);

        // when
        memberManager.updatePassword(member, newPassword);

        // then
        assertThat(member.getPassword()).isEqualTo(new Password(encodedNewPassword));
    }

    @Test
    void 관리자가_회원을_정지하면_status_role_refreshToken이_변경되고_제한_기록이_저장된다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        member.updateRefreshToken("existing-token");
        given(memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)).willReturn(Optional.of(member));

        // when
        memberManager.suspend(memberId, "규칙 위반");

        // then
        assertThat(member.getStatus()).isEqualTo(EntityStatus.SUSPENDED);
        assertThat(member.getRole()).isEqualTo(Role.ROLE_SUSPEND_MEMBER);
        assertThat(member.getRefreshToken()).isNull();
        verify(memberRestrictionJpaRepository).save(any(MemberRestriction.class));
        verify(suspensionManager).suspend(memberId);
    }

    @Test
    void 회원_탈퇴_시_status가_DELETED로_변경되고_소유_엔티티가_삭제된다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        member.updateRefreshToken("existing-token");
        given(memberJpaRepository.findByIdAndStatusNot(memberId, EntityStatus.DELETED)).willReturn(Optional.of(member));

        // when
        memberManager.withdraw(memberId);

        // then
        assertThat(member.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(member.getRefreshToken()).isNull();
        verify(memberDeviceManager).deleteAllByMemberId(memberId);
        verify(memberProfileTagManager).deleteByMemberId(memberId);
    }

    @Test
    void 정지된_회원도_탈퇴가_가능하다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        member.suspend();
        given(memberJpaRepository.findByIdAndStatusNot(memberId, EntityStatus.DELETED)).willReturn(Optional.of(member));

        // when
        memberManager.withdraw(memberId);

        // then
        assertThat(member.getStatus()).isEqualTo(EntityStatus.DELETED);
        verify(memberDeviceManager).deleteAllByMemberId(memberId);
        verify(memberProfileTagManager).deleteByMemberId(memberId);
    }

    @Test
    void 탈퇴_대상_회원이_존재하지_않으면_예외가_발생한다() {
        // given
        given(memberJpaRepository.findByIdAndStatusNot(999L, EntityStatus.DELETED)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> memberManager.withdraw(999L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 정지_대상_회원이_존재하지_않으면_예외가_발생한다() {
        // given
        given(memberJpaRepository.findByIdAndStatus(999L, EntityStatus.ACTIVE)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> memberManager.suspend(999L, "규칙 위반"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

}