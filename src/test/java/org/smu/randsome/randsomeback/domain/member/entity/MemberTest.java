package org.smu.randsome.randsomeback.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.vo.MyProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.jwt.TokenHasher;

class MemberTest extends UnitTestSupport {

    Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.create();
    }

    @Test
    void 회원을_생성하면_이메일과_기본_정보가_설정된다() {
        assertThat(member).isNotNull().extracting(
                Member::getEmail,
                Member::getLegalName,
                Member::getRole
        ).containsExactly(
                MemberFixture.email(),
                MemberFixture.DEFAULT_LEGAL_NAME,
                Role.ROLE_MEMBER
        );
        assertThat(member.getMbti()).isNotNull();
    }

    @Test
    void 태그와_함께_회원을_생성하면_소개_태그가_설정된다() {
        Member memberWithTags = MemberFixture.create();

        MyProfileTags tags = memberWithTags.getMyProfileTags();
        assertThat(tags).isNotNull();
        assertThat(tags.personalityTag()).isEqualTo(MemberFixture.DEFAULT_PERSONALITY_TAG);
        assertThat(tags.faceTypeTag()).isEqualTo(MemberFixture.DEFAULT_FACE_TYPE_TAG);
        assertThat(tags.datingStyleTag()).isEqualTo(MemberFixture.DEFAULT_DATING_STYLE_TAG);
    }

    @Test
    void 회원_생성_시_비밀번호가_해시화된다() {
        assertThat(member.isPasswordCorrect(MemberFixture.DEFAULT_RAW_PASSWORD, MemberFixture.ENCODER)).isTrue();
    }

    @Test
    void 회원_생성_시_소셜_프로필이_설정된다() {
        SocialProfile socialProfile = member.getSocialProfile();

        assertThat(socialProfile).isNotNull().extracting(
                SocialProfile::instagramId,
                SocialProfile::selfIntroduction,
                SocialProfile::idealDescription
        ).containsExactly(
                MemberFixture.DEFAULT_INSTAGRAM_ID,
                MemberFixture.DEFAULT_SELF_INTRODUCTION,
                MemberFixture.DEFAULT_IDEAL_DESCRIPTION
        );
    }

    @Test
    void 소개_태그를_변경한다() {
        MyProfileTags newTags = MyProfileTags.create(PersonalityTag.QUIET, FaceTypeTag.CAT, DatingStyleTag.GROW_TOGETHER);

        member.changeProfileTags(newTags);

        assertThat(member.getMyProfileTags()).isNotNull().extracting(
                MyProfileTags::personalityTag,
                MyProfileTags::faceTypeTag,
                MyProfileTags::datingStyleTag
        ).containsExactly(PersonalityTag.QUIET, FaceTypeTag.CAT, DatingStyleTag.GROW_TOGETHER);
    }

    @Test
    void MEMBER에서_ADMIN으로_역할_전환이_가능하다() {
        member.updateRole(Role.ROLE_ADMIN);

        assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void 리프레시_토큰을_갱신한다() {
        member.updateRefreshToken("refresh-token");

        assertThat(member.getRefreshToken()).isEqualTo(TokenHasher.hash("refresh-token"));
    }

    @Test
    void revokeRefreshToken_호출_시_토큰이_null로_초기화된다() {
        member.updateRefreshToken("some-token");

        member.revokeRefreshToken();

        assertThat(member.getRefreshToken()).isNull();
    }

    @Test
    void 프로필을_업데이트한다() {
        // given
        var newLegalName = "김철수";
        var newMbti = Mbti.ENFP;
        var department = Department.SOFTWARE;
        var newInstagramId = "new_insta";
        var newSelfIntroduction = "새 자기소개";
        var newIdealDescription = "새 이상형";
        var personalityTag = PersonalityTag.QUIET;
        var faceTypeTag = FaceTypeTag.CAT;
        var datingStyleTag = DatingStyleTag.GROW_TOGETHER;

        // when
        member.updateProfile(
                newLegalName,
                newMbti,
                department,
                newInstagramId,
                newSelfIntroduction,
                newIdealDescription,
                personalityTag,
                faceTypeTag,
                datingStyleTag
        );

        // then
        assertThat(member.getLegalName()).isEqualTo(newLegalName);
        assertThat(member.getMbti()).isEqualTo(newMbti);
        assertThat(member.getSocialProfile()).isNotNull().extracting(
                SocialProfile::instagramId,
                SocialProfile::selfIntroduction,
                SocialProfile::idealDescription
        ).containsExactly(newInstagramId, newSelfIntroduction, newIdealDescription);
    }

    @Test
    void 틀린_비밀번호_검증_시_false를_반환한다() {
        assertThat(member.isPasswordCorrect("wrongPassword!", MemberFixture.ENCODER)).isFalse();
    }

    @Test
    void 비밀번호를_변경하면_새_비밀번호로_인증된다() {
        String newPassword = "newPassword123!";

        member.updatePassword(newPassword, MemberFixture.ENCODER);

        assertThat(member.isPasswordCorrect(newPassword, MemberFixture.ENCODER)).isTrue();
    }

    @Test
    void 비밀번호를_변경하면_이전_비밀번호로_인증이_실패한다() {
        String newPassword = "newPassword123!";

        member.updatePassword(newPassword, MemberFixture.ENCODER);

        assertThat(member.isPasswordCorrect(MemberFixture.DEFAULT_RAW_PASSWORD, MemberFixture.ENCODER)).isFalse();
    }

}
