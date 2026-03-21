package org.smu.randsome.randsomeback.domain.member.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Email;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.entity.vo.StudentId;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Column(unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String legalName;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Embedded
    private StudentId studentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    @Embedded
    private SocialProfile socialProfile;

    private String refreshToken;

    public static Member create(
            String email,
            String rawPassword,
            PasswordEncoder encoder,
            String legalName,
            Gender gender,
            Mbti mbti,
            String instagramId,
            String selfIntroduction,
            String idealDescription
    ) {
        Member member = new Member();

        String safeEmail = requireNonNull(email);
        Gender safeGender = requireNonNull(gender);

        member.email = new Email(safeEmail);
        member.nickname = createRandomNickname(safeGender);
        member.legalName = requireNonNull(legalName);
        member.password = Password.create(rawPassword, encoder);
        member.gender = safeGender;
        member.mbti = requireNonNull(mbti);
        member.studentId = StudentId.create(safeEmail);
        member.socialProfile = SocialProfile.create(instagramId, selfIntroduction, idealDescription);
        member.role = Role.ROLE_MEMBER;
        member.refreshToken = null;

        return member;
    }

    public void updateRole(Role newRole) {
        this.role = requireNonNull(newRole);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void revokeRefreshToken() {
        this.refreshToken = null;
    }

    public boolean isPasswordCorrect(String rawPassword, PasswordEncoder encoder) {
        return password.matches(rawPassword, encoder);
    }

    public void updateProfile(
            String legalName,
            Mbti mbti,
            String instagramId,
            String selfIntroduction,
            String idealDescription
    ) {
        this.legalName = requireNonNull(legalName);
        this.mbti = requireNonNull(mbti);
        this.socialProfile = SocialProfile.create(instagramId, selfIntroduction, idealDescription);
    }

    public boolean isAdmin() {
        return role.equals(Role.ROLE_ADMIN);
    }

    private static String createRandomNickname(Gender gender) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        return gender.getValue() + "#" + suffix;
    }

    public void updatePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.password = Password.create(newPassword, passwordEncoder);
    }

}