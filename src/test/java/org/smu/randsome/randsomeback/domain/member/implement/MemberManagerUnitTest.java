package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.MyProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
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
    PasswordEncoder passwordEncoder;

    @Test
    void 태그와_함께_회원을_생성한다() {
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
        MyProfileTags tags = member.getMyProfileTags();
        assertThat(tags).isNotNull().extracting(
                MyProfileTags::personalityTag,
                MyProfileTags::faceTypeTag,
                MyProfileTags::datingStyleTag

        ).containsExactly(
                MemberFixture.DEFAULT_PERSONALITY_TAG,
                MemberFixture.DEFAULT_FACE_TYPE_TAG,
                MemberFixture.DEFAULT_DATING_STYLE_TAG
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
        Member member = MemberFixture.create();
        String newPassword = "newPassword123!";
        String encodedNewPassword = "encoded-new-password";
        given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);

        // when
        memberManager.updatePassword(member, newPassword);

        // then
        assertThat(member.getPassword()).isEqualTo(new Password(encodedNewPassword));
    }

}