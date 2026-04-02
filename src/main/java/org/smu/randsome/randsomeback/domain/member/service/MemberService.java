package org.smu.randsome.randsomeback.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.bankaccount.dto.command.BankAccountInfo;
import org.smu.randsome.randsomeback.domain.bankaccount.dto.command.UpdateBankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.implement.BankAccountManager;
import org.smu.randsome.randsomeback.domain.bankaccount.implement.BankAccountReader;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.terms.implement.TermsAgreementManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberManager memberManager;
    private final MemberReader memberReader;
    private final MemberValidator memberValidator;
    private final TermsAgreementManager termsAgreementManager;
    private final BankAccountManager bankAccountManager;
    private final BankAccountReader bankAccountReader;

    /**
     * 회원 가입을 처리하는 서비스 메서드입니다.
     *
     * @param emailVerificationToken 이메일 인증 토큰
     * @param credentials            회원의 계정 정보 (이메일, 비밀번호)
     * @param basicInfo              회원의 기본 정보 (이름, 생년월일 등)
     * @param socialProfile          회원의 소셜 프로필 정보 (인스타그램 ID, 자기소개 등)
     * @param bankAccountInfo        회원의 은행 계좌 정보
     * @return 생성된 회원의 ID
     *
     */
    @Transactional
    public Long create(
            String emailVerificationToken,
            MemberCredentials credentials,
            MemberBasicInfo basicInfo,
            MemberSocialProfile socialProfile,
            BankAccountInfo bankAccountInfo
    ) {
        memberValidator.validateSignUpToken(emailVerificationToken, credentials.email());

        Member member = memberManager.create(credentials, basicInfo, socialProfile);
        termsAgreementManager.saveAll(member.getId());
        bankAccountManager.create(member.getId(), bankAccountInfo);

        log.info("[MemberService] 회원가입 완료 - memberId={}", member.getId());

        return member.getId();
    }

    @Transactional(readOnly = true)
    public Member getMyProfile(Long memberId) {
        return memberReader.find(memberId);
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfile updateProfile, UpdateBankAccount updateBankAccount) {
        memberManager.updateProfile(memberId, updateProfile);
        bankAccountManager.update(bankAccountReader.findByMemberId(memberId), updateBankAccount);
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