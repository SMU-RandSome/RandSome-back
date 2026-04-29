package org.smu.randsome.randsomeback.domain.member.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Email;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.entity.vo.StudentId;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.jwt.TokenHasher;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"email", "status"}))
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

    @Embedded
    private SocialProfile socialProfile;

    private String refreshToken;

    @Version
    private Long version;

    public static Member create(
            String email,
            String rawPassword,
            PasswordEncoder encoder,
            String legalName,
            Gender gender,
            Mbti mbti,
            Department department,
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
        member.department = requireNonNull(department);
        member.studentId = StudentId.create(safeEmail);
        member.socialProfile = SocialProfile.create(instagramId, selfIntroduction, idealDescription);
        member.role = Role.ROLE_MEMBER;
        member.refreshToken = null;

        return member;
    }

    @Override
    public void suspend() {
        super.suspend();
        this.role = Role.ROLE_SUSPEND_MEMBER;
        revokeRefreshToken();
    }

    public void withdraw() {
        delete();
        revokeRefreshToken();
    }

    public void updateRole(Role newRole) {
        this.role = requireNonNull(newRole);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = TokenHasher.hash(refreshToken);
    }

    public void revokeRefreshToken() {
        this.refreshToken = null;
    }

    public boolean isPasswordCorrect(String rawPassword, PasswordEncoder encoder) {
        return password.matches(rawPassword, encoder);
    }

    public boolean isEmailCorrect(String tokenEmail) {
        return email.address().equals(tokenEmail);
    }

    public boolean isAdmin() {
        return role.equals(Role.ROLE_ADMIN);
    }

    public void updateProfile(
            String legalName,
            Mbti mbti,
            Department department,
            String instagramId,
            String selfIntroduction,
            String idealDescription
    ) {
        this.legalName = requireNonNull(legalName);
        this.mbti = requireNonNull(mbti);
        this.department = requireNonNull(department);
        this.socialProfile = SocialProfile.create(instagramId, selfIntroduction, idealDescription);
    }

    public void updatePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.password = Password.create(newPassword, passwordEncoder);
    }

    private static String createRandomNickname(Gender gender) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        return gender.getValue() + "#" + suffix;
    }

}