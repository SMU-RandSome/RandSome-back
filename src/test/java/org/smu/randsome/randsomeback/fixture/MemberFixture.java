package org.smu.randsome.randsomeback.fixture;

import org.smu.randsome.randsomeback.domain.member.dto.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberTagsInfo;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Email;
import org.smu.randsome.randsomeback.domain.member.entity.vo.MyProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.entity.vo.StudentId;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberFixture {

    public static final String DEFAULT_EMAIL             = "202312345@sangmyung.kr";
    public static final String DEFAULT_LEGAL_NAME        = "홍길동";
    public static final String DEFAULT_RAW_PASSWORD      = "password123!";
    public static final String DEFAULT_INSTAGRAM_ID      = "my_insta";
    public static final String DEFAULT_SELF_INTRODUCTION = "안녕하세요";
    public static final String DEFAULT_IDEAL_DESCRIPTION = "착한 사람";
    public static final Mbti DEFAULT_MBTI                = Mbti.ISTP;
    public static final Gender DEFAULT_GENDER            = Gender.MALE;
    public static final Department DEFAULT_DEPARTMENT    = Department.SOFTWARE;
    public static final Department OTHER_DEPARTMENT      = Department.ELECTRONICS_ENGINEERING;

    public static final PersonalityTag DEFAULT_PERSONALITY_TAG    = PersonalityTag.ACTIVE;
    public static final FaceTypeTag DEFAULT_FACE_TYPE_TAG         = FaceTypeTag.PUPPY;
    public static final DatingStyleTag DEFAULT_DATING_STYLE_TAG   = DatingStyleTag.EXPRESSIVE;

    public static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static Member create() {
        return Member.create(
                DEFAULT_EMAIL,
                DEFAULT_RAW_PASSWORD,
                ENCODER,
                DEFAULT_LEGAL_NAME,
                DEFAULT_GENDER,
                DEFAULT_MBTI,
                DEFAULT_DEPARTMENT,
                DEFAULT_INSTAGRAM_ID,
                DEFAULT_SELF_INTRODUCTION,
                DEFAULT_IDEAL_DESCRIPTION,
                createMyProfileTags()
        );
    }

    public static MemberCredentials createCredentials() {
        return new MemberCredentials(DEFAULT_EMAIL, DEFAULT_RAW_PASSWORD);
    }

    public static MemberBasicInfo createBasicInfo() {
        return new MemberBasicInfo(DEFAULT_LEGAL_NAME, DEFAULT_GENDER, DEFAULT_MBTI, DEFAULT_DEPARTMENT);
    }

    public static MemberSocialProfile createMemberSocialProfile() {
        return new MemberSocialProfile(DEFAULT_INSTAGRAM_ID, DEFAULT_SELF_INTRODUCTION, DEFAULT_IDEAL_DESCRIPTION);
    }

    public static MemberTagsInfo createTagsInfo() {
        return new MemberTagsInfo(DEFAULT_PERSONALITY_TAG, DEFAULT_FACE_TYPE_TAG, DEFAULT_DATING_STYLE_TAG);
    }

    public static MyProfileTags createMyProfileTags() {
        return MyProfileTags.of(DEFAULT_PERSONALITY_TAG, DEFAULT_FACE_TYPE_TAG, DEFAULT_DATING_STYLE_TAG);
    }

    public static Email email() {
        return new Email(DEFAULT_EMAIL);
    }

    public static Password password() {
        return Password.create(DEFAULT_RAW_PASSWORD, ENCODER);
    }

    public static Password password(String raw) {
        return Password.create(raw, ENCODER);
    }

    public static StudentId studentId() {
        return StudentId.create(DEFAULT_EMAIL);
    }

    public static Member createWithLegalName(String email, String legalName) {
        return Member.create(
                email,
                DEFAULT_RAW_PASSWORD,
                ENCODER,
                legalName,
                DEFAULT_GENDER,
                DEFAULT_MBTI,
                DEFAULT_DEPARTMENT,
                email.substring(0, email.indexOf('@')),
                DEFAULT_SELF_INTRODUCTION,
                DEFAULT_IDEAL_DESCRIPTION,
                createMyProfileTags()
        );
    }

    public static Member createWithGender(String email, Gender gender) {
        String instagramId = email.substring(0, email.indexOf('@'));
        return Member.create(
                email,
                DEFAULT_RAW_PASSWORD,
                ENCODER,
                DEFAULT_LEGAL_NAME,
                gender,
                DEFAULT_MBTI,
                DEFAULT_DEPARTMENT,
                instagramId,
                DEFAULT_SELF_INTRODUCTION,
                DEFAULT_IDEAL_DESCRIPTION,
                createMyProfileTags()
        );
    }

    public static Member createCandidate(String email, Gender gender) {
        Member member = createWithGender(email, gender);
        member.updateRole(Role.ROLE_CANDIDATE);
        return member;
    }

    public static Member createCandidateWithDepartment(String email, Gender gender, Department department) {
        String instagramId = email.substring(0, email.indexOf('@'));
        Member member = Member.create(
                email,
                DEFAULT_RAW_PASSWORD,
                ENCODER,
                DEFAULT_LEGAL_NAME,
                gender,
                DEFAULT_MBTI,
                department,
                instagramId,
                DEFAULT_SELF_INTRODUCTION,
                DEFAULT_IDEAL_DESCRIPTION,
                createMyProfileTags()
        );
        member.updateRole(Role.ROLE_CANDIDATE);
        return member;
    }

    public static SocialProfile socialProfile() {
        return SocialProfile.create(DEFAULT_INSTAGRAM_ID, DEFAULT_SELF_INTRODUCTION, DEFAULT_IDEAL_DESCRIPTION);
    }

}