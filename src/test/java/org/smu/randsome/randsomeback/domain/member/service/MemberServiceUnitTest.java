package org.smu.randsome.randsomeback.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.bankaccount.implement.BankAccountManager;
import org.smu.randsome.randsomeback.domain.bankaccount.service.command.BankAccountInfo;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.terms.implement.TermsAgreementManager;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class MemberServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberService memberService;

    @Mock
    MemberManager memberManager;

    @Mock
    MemberValidator memberValidator;

    @Mock
    TermsAgreementManager termsAgreementManager;

    @Mock
    BankAccountManager bankAccountManager;

    @Test
    void 회원가입에_성공하면_회원_ID를_반환한다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(memberManager.create(any(), any(), any())).willReturn(member);

        var bankAccountInfo = createBankAccountInfo();

        // when
        Long memberId = memberService.create(
                "email.verification.token",
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                bankAccountInfo
        );

        // then
        assertThat(memberId).isEqualTo(1L);
        verify(memberValidator).validateSignUpToken("email.verification.token", MemberFixture.DEFAULT_EMAIL);
        verify(termsAgreementManager).saveAll(1L);
        verify(bankAccountManager).create(1L, bankAccountInfo);
    }

    @Test
    void 이메일_인증_토큰이_유효하지_않으면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.INVALID_SIGNUP_REQUEST))
                .given(memberValidator).validateSignUpToken(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.create(
                "invalid.token",
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                createBankAccountInfo()
        )).isInstanceOf(CoreException.class)
          .hasMessage(ErrorType.INVALID_SIGNUP_REQUEST.getMessage());
    }

    private BankAccountInfo createBankAccountInfo() {
        return new BankAccountInfo("국민은행", "123456789012", MemberFixture.DEFAULT_LEGAL_NAME);
    }

}
