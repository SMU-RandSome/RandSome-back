package org.smu.randsome.randsomeback.domain.member.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberTagsInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberProfileTagReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.terms.implement.TermsAgreementManager;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberManager memberManager;
    private final MemberReader memberReader;
    private final MemberProfileTagReader memberProfileTagReader;
    private final MemberValidator memberValidator;
    private final CandidateManager candidateManager;
    private final TermsAgreementManager termsAgreementManager;
    private final TicketHandler ticketHandler;

    /**
     * 회원 가입을 처리하는 서비스 메서드입니다.
     *
     * @param emailVerificationToken 이메일 인증 토큰
     * @param credentials            회원의 계정 정보 (이메일, 비밀번호)
     * @param basicInfo              회원의 기본 정보 (이름, 생년월일 등)
     * @param socialProfile          회원의 소셜 프로필 정보 (인스타그램 ID, 자기소개 등)
     * @param tagsInfo               회원의 태그 정보 (성격, 얼굴형, 데이트 스타일 등)
     * @return 생성된 회원의 ID
     *
     */
    @Transactional
    public Long create(
            String emailVerificationToken,
            MemberCredentials credentials,
            MemberBasicInfo basicInfo,
            MemberSocialProfile socialProfile,
            MemberTagsInfo tagsInfo
    ) {
        memberValidator.validateSignUpToken(emailVerificationToken, credentials.email());

        Member member = memberManager.create(credentials, basicInfo, socialProfile, tagsInfo);
        termsAgreementManager.saveAll(member.getId());
        ticketHandler.issue(member);

        log.info("[MemberService] 회원가입 완료 - memberId={}", member.getId());

        return member.getId();
    }

    @Transactional(readOnly = true)
    public Member getMyProfile(Long memberId) {
        return memberReader.find(memberId);
    }

    @Transactional(readOnly = true)
    public MemberProfileTag getProfileTag(Long memberId) {
        return memberProfileTagReader.find(memberId);
    }

    @Transactional(readOnly = true)
    public Map<Long, MemberProfileTag> getProfileTags(List<Long> memberIds) {
        return memberProfileTagReader.findAllByMemberIds(memberIds);
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfile updateProfile) {
        memberManager.updateProfile(memberId, updateProfile);
    }

    @Transactional
    public void withdraw(Long memberId, String rawPassword) {
        Member member = memberReader.findNonDeleted(memberId);
        memberValidator.validateWithdraw(member, rawPassword);

        candidateManager.withdrawAllByMemberId(memberId);
        memberManager.withdraw(memberId);
    }

    /**
     * 회원의 비밀번호를 업데이트하는 서비스 메서드입니다.
     * @param newPassword            새로운 비밀번호
     * @param emailVerificationToken 이메일 인증 토큰 (비밀번호 재설정 용도)
     * @param email                  회원의 이메일 주소
     * */
    @Transactional
    public void updatePassword(String newPassword, String emailVerificationToken, String email) {
        Member member = memberReader.findByEmail(email);
        memberValidator.validateUpdatePassword(emailVerificationToken, member);

        memberManager.updatePassword(member, newPassword);
    }

}