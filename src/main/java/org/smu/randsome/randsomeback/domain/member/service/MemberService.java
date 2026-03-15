package org.smu.randsome.randsomeback.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.bankaccount.implement.BankAccountManager;
import org.smu.randsome.randsomeback.domain.bankaccount.service.command.BankAccountInfo;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.terms.implement.TermsAgreementManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberManager memberManager;
    private final MemberReader memberReader;
    private final MemberValidator memberValidator;
    private final TermsAgreementManager termsAgreementManager;
    private final BankAccountManager bankAccountManager;

    /**
     * 회원 가입을 처리하는 서비스 메서드입니다.
     * @param emailVerificationToken 이메일 인증 토큰
     * @param credentials 회원의 계정 정보 (이메일, 비밀번호)
     * @param basicInfo 회원의 기본 정보 (이름, 생년월일 등)
     * @param socialProfile 회원의 소셜 프로필 정보 (인스타그램 ID, 자기소개 등)
     * @param bankAccountInfo 회원의 은행 계좌 정보
     * @return 생성된 회원의 ID
     * */
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

        return member.getId();
    }

    @Transactional(readOnly = true)
    public Member getMyProfile(Long memberId) {
        return memberReader.find(memberId);
    }

}