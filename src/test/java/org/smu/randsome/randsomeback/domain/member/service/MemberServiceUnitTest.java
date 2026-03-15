package org.smu.randsome.randsomeback.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.member.service.command.UpdateProfile;
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
    MemberReader memberReader;

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

    @Test
    void 내_프로필_조회에_성공하면_Member를_반환한다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.find(1L)).willReturn(member);

        // when
        Member result = memberService.getMyProfile(1L);

        // then
        assertThat(result).isEqualTo(member);
    }

    @Test
    void 존재하지_않는_회원이면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberReader).find(any());

        // when & then
        assertThatThrownBy(() -> memberService.getMyProfile(999L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 프로필_업데이트에_성공한다() {
        // given
        var updateProfile = UpdateProfile.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .instagramId("new_insta")
                .selfIntroduction("새 자기소개")
                .idealDescription("새 이상형")
                .build();

        // when
        memberService.updateProfile(1L, updateProfile);

        // then
        verify(memberManager).updateProfile(eq(1L), eq(updateProfile));
    }

    @Test
    void 프로필_업데이트_시_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        var updateProfile = UpdateProfile.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .build();

        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberManager).updateProfile(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.updateProfile(999L, updateProfile))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    private BankAccountInfo createBankAccountInfo() {
        return new BankAccountInfo("국민은행", "123456789012", MemberFixture.DEFAULT_LEGAL_NAME);
    }

}
