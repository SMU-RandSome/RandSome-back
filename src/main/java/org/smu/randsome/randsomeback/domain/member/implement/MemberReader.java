package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.jwt.TokenHasher;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberReader {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public Member findByAccount(String loginId, String password) {
        Member member = memberJpaRepository.findByEmail_AddressAndStatus(loginId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.INVALID_ACCOUNT));

        if (member.isPasswordCorrect(password, passwordEncoder)) {
            return member;
        }

        throw new CoreException(ErrorType.INVALID_ACCOUNT);
    }

    public Member find(Long memberId) {
        return memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Member getReference(Long memberId) {
        if (!memberJpaRepository.existsByIdAndStatus(memberId, EntityStatus.ACTIVE)) {
            throw new CoreException(ErrorType.NOT_FOUND_MEMBER);
        }
        return memberJpaRepository.getReferenceById(memberId);
    }

    public Member findWithLock(Long memberId) {
        return memberJpaRepository.findByIdAndStatusWithLock(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Page<Member> findAll(Pageable pageable) {
        return memberJpaRepository.findAllByStatusAndRoleNot(EntityStatus.ACTIVE, Role.ROLE_ADMIN, pageable);
    }

    public List<Member> findCandidatesByGender(Gender gender, Department excludeDepartment, int count) {
        // NOTE: 자율 전공일 경우, 학과를 제외하지 않고 조회한다.
        if (excludeDepartment.isSelfDirectedMajor()) {
            return memberJpaRepository.findRandomCandidatesByGender(gender.name(), count);
        }
        return memberJpaRepository.findRandomCandidatesByGenderExcludingDepartment(
                gender.name(), excludeDepartment.name(), count);
    }

    /**
     * 이상형 매칭 태그 스코어링을 위해 대상 성별의 전체 후보군을 조회한다.
     * 무작위 정렬 없이 전체를 반환하며, 동점 처리는 호출 측에서 셔플로 수행한다.
     *
     * @param gender            대상 성별
     * @param excludeDepartment 제외할 학과 (자율 전공은 제외 없음)
     * @return 후보군 전체 목록
     */
    public List<Member> findAllCandidatesByGender(Gender gender, Department excludeDepartment) {
        if (excludeDepartment.isSelfDirectedMajor()) {
            return memberJpaRepository.findAllCandidatesByGender(gender, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);
        }
        return memberJpaRepository.findAllCandidatesByGenderExcludingDepartment(
                gender, excludeDepartment, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);
    }

    public Member findByRefreshToken(String refreshToken) {
        return memberJpaRepository.findByRefreshTokenAndStatus(TokenHasher.hash(refreshToken), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN));
    }

    public Member findByEmail(String email) {
        return memberJpaRepository.findByEmail_AddressAndStatus(email, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

}