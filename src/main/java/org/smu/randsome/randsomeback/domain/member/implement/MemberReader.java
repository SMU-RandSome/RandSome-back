package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.dto.CandidateIdMbti;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSearchCondition;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.jwt.TokenHasher;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class MemberReader {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member findByAccount(String loginId, String password) {
        Member member = memberRepository.findByEmail_AddressAndStatus(loginId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.INVALID_ACCOUNT));

        if (member.isPasswordCorrect(password, passwordEncoder)) {
            return member;
        }

        throw new CoreException(ErrorType.INVALID_ACCOUNT);
    }

    public Member find(Long memberId) {
        return memberRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Member getReference(Long memberId) {
        if (!memberRepository.existsByIdAndStatus(memberId, EntityStatus.ACTIVE)) {
            throw new CoreException(ErrorType.NOT_FOUND_MEMBER);
        }
        return memberRepository.getReferenceById(memberId);
    }

    public Member findWithLock(Long memberId) {
        return memberRepository.findByIdAndStatusWithLock(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Member findNonDeleted(Long memberId) {
        return memberRepository.findByIdAndStatusNot(memberId, EntityStatus.DELETED)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public PageResponse<Member> findAll(MemberSearchCondition condition, OffsetLimit offsetLimit) {
        List<Member> items = memberRepository.findAllNonDeletedNonAdmin(condition, offsetLimit.offset(), offsetLimit.limit());
        long total = memberRepository.countNonDeletedNonAdmin(condition);

        return PageResponse.of(items, offsetLimit.page(), offsetLimit.size(), total);
    }

    /**
     * 대상 성별의 후보 ID를 조회한 뒤, 앱 레벨에서 셔플하여 count만큼 엔티티를 반환한다.
     * ORDER BY RAND() 대신 앱 레벨 랜덤을 사용하여 DB의 임시 테이블 + filesort를 제거한다.
     */
    public List<Member> findCandidatesByGender(Gender gender, Department excludeDepartment, int count) {
        List<Long> candidateIds = findCandidateIds(gender, excludeDepartment);

        Collections.shuffle(candidateIds);
        List<Long> selectedIds = candidateIds.subList(0, Math.min(count, candidateIds.size()));

        return memberRepository.findAllById(selectedIds);
    }

    private List<Long> findCandidateIds(Gender gender, Department excludeDepartment) {
        if (excludeDepartment.isSelfDirectedMajor()) {
            return new ArrayList<>(memberRepository.findCandidateIdsByGender(
                    gender, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE));
        }
        return new ArrayList<>(memberRepository.findCandidateIdsByGenderExcludingDepartment(
                gender, excludeDepartment, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE));
    }

    /**
     * 이상형 매칭 스코어링을 위해 후보 ID와 MBTI만 경량 조회한다.
     * Member 엔티티 전체 로딩 대신 2컬럼만 조회하여 DB 전송량과 JPA 하이드레이션 비용을 절감한다.
     */
    public Map<Long, Mbti> findCandidateIdMbtiMap(Gender gender, Department excludeDepartment) {
        List<CandidateIdMbti> candidates;
        if (excludeDepartment.isSelfDirectedMajor()) {
            candidates = memberRepository.findCandidateIdAndMbtiByGender(
                    gender,
                    Role.ROLE_CANDIDATE,
                    EntityStatus.ACTIVE
            );
        } else {
            candidates = memberRepository.findCandidateIdAndMbtiByGenderExcludingDepartment(
                    gender,
                    excludeDepartment,
                    Role.ROLE_CANDIDATE,
                    EntityStatus.ACTIVE
            );
        }
        return candidates.stream().collect(Collectors.toMap(
                CandidateIdMbti::id,
                CandidateIdMbti::mbti
        ));
    }

    public Map<Long, Member> findAllByIds(List<Long> memberIds) {
        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
    }

    public Member findByRefreshToken(String refreshToken) {
        return memberRepository.findByRefreshTokenAndStatus(TokenHasher.hash(refreshToken), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail_AddressAndStatus(email, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

}
