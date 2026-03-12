package org.smu.randsome.randsomeback.fixture;

import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Email;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.entity.vo.StudentId;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberSocialProfile;
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

    public static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static Member create() {
        return Member.create(
                DEFAULT_EMAIL,
                DEFAULT_RAW_PASSWORD,
                ENCODER,
                DEFAULT_LEGAL_NAME,
                DEFAULT_GENDER,
                DEFAULT_MBTI,
                DEFAULT_INSTAGRAM_ID,
                DEFAULT_SELF_INTRODUCTION,
                DEFAULT_IDEAL_DESCRIPTION
        );
    }

    public static MemberCredentials createCredentials() {
        return new MemberCredentials(DEFAULT_EMAIL, DEFAULT_RAW_PASSWORD);
    }

    public static MemberBasicInfo createBasicInfo() {
        return new MemberBasicInfo(DEFAULT_LEGAL_NAME, DEFAULT_GENDER, DEFAULT_MBTI);
    }

    public static MemberSocialProfile createMemberSocialProfile() {
        return new MemberSocialProfile(DEFAULT_INSTAGRAM_ID, DEFAULT_SELF_INTRODUCTION, DEFAULT_IDEAL_DESCRIPTION);
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


    public static Member createWithGender(String email, Gender gender) {
        String instagramId = email.substring(0, email.indexOf('@'));
        return Member.create(
                email,
                DEFAULT_RAW_PASSWORD,
                ENCODER,
                DEFAULT_LEGAL_NAME,
                gender,
                DEFAULT_MBTI,
                instagramId,
                DEFAULT_SELF_INTRODUCTION,
                DEFAULT_IDEAL_DESCRIPTION
        );
    }

    public static Member createCandidate(String email, Gender gender) {
        Member member = createWithGender(email, gender);
        member.updateRole(Role.ROLE_CANDIDATE);
        return member;
    }

    public static SocialProfile socialProfile() {
        return SocialProfile.create(DEFAULT_INSTAGRAM_ID, DEFAULT_SELF_INTRODUCTION, DEFAULT_IDEAL_DESCRIPTION);
    }

}