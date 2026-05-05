package org.smu.randsome.randsomeback.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.terms.implement.TermsAgreementManager;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
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
    CandidateManager candidateManager;

    @Mock
    TermsAgreementManager termsAgreementManager;

    @Mock
    TicketHandler ticketHandler;

    @Test
    void 회원가입에_성공하면_회원_ID를_반환한다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(memberManager.create(any(), any(), any(), any())).willReturn(member);


        // when
        Long memberId = memberService.create(
                "email.verification.token",
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                MemberFixture.createTagsInfo()
        );

        // then
        assertThat(memberId).isEqualTo(1L);
        verify(memberValidator).validateSignUpToken("email.verification.token", MemberFixture.DEFAULT_EMAIL);
        verify(termsAgreementManager).saveAll(1L);
        verify(ticketHandler).issue(member);
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
                MemberFixture.createTagsInfo()
        )).isInstanceOf(CoreException.class)
          .hasMessage(ErrorType.INVALID_SIGNUP_REQUEST.getMessage());
    }

    @Test
    void 회원가입_시_토큰_목적이_유효하지_않으면_예외가_발생하고_후속_로직을_수행하지_않는다() {
        // given
        willThrow(new CoreException(ErrorType.INVALID_VERIFICATION_PURPOSE))
                .given(memberValidator).validateSignUpToken(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.create(
                "invalid.purpose.token",
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                MemberFixture.createTagsInfo()
        )).isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_VERIFICATION_PURPOSE.getMessage());

        verifyNoInteractions(memberManager, termsAgreementManager);
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

    @Test
    void 비밀번호_변경에_성공한다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.findByEmail(MemberFixture.DEFAULT_EMAIL)).willReturn(member);

        // when
        memberService.updatePassword("newPassword123!", "password.verification.token", MemberFixture.DEFAULT_EMAIL);

        // then
        verify(memberValidator).validateUpdatePassword("password.verification.token", member);
        verify(memberManager).updatePassword(member, "newPassword123!");
    }

    @Test
    void 비밀번호_변경_시_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberReader).findByEmail(any());

        // when & then
        assertThatThrownBy(() -> memberService.updatePassword("newPassword123!", "password.verification.token", "unknown@sangmyung.kr"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 비밀번호_변경_시_이메일_인증_토큰이_유효하지_않으면_예외가_발생한다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.findByEmail(MemberFixture.DEFAULT_EMAIL)).willReturn(member);
        willThrow(new CoreException(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST))
                .given(memberValidator).validateUpdatePassword(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.updatePassword("newPassword123!", "invalid.token", MemberFixture.DEFAULT_EMAIL))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST.getMessage());
    }

    @Test
    void 회원_탈퇴에_성공한다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.findNonDeleted(1L)).willReturn(member);

        // when
        memberService.withdraw(1L, "password123!");

        // then
        verify(memberValidator).validateWithdraw(member, "password123!");
        verify(candidateManager).withdrawAllByMemberId(1L);
        verify(memberManager).withdraw(1L);
    }

    @Test
    void 탈퇴_시_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberReader).findNonDeleted(any());

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(999L, "password123!"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 탈퇴_시_관리자면_예외가_발생하고_후속_로직을_수행하지_않는다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.findNonDeleted(1L)).willReturn(member);
        willThrow(new CoreException(ErrorType.ADMIN_CANNOT_WITHDRAW))
                .given(memberValidator).validateWithdraw(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(1L, "password123!"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ADMIN_CANNOT_WITHDRAW.getMessage());

        verifyNoInteractions(candidateManager, memberManager);
    }

    @Test
    void 탈퇴_시_비밀번호가_일치하지_않으면_예외가_발생한다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.findNonDeleted(1L)).willReturn(member);
        willThrow(new CoreException(ErrorType.INCORRECT_PASSWORD))
                .given(memberValidator).validateWithdraw(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(1L, "wrongPassword!"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INCORRECT_PASSWORD.getMessage());

        verifyNoInteractions(candidateManager, memberManager);
    }

    @Test
    void 비밀번호_변경_시_토큰_목적이_유효하지_않으면_예외가_발생하고_비밀번호를_변경하지_않는다() {
        // given
        Member member = mock(Member.class);
        given(memberReader.findByEmail(MemberFixture.DEFAULT_EMAIL)).willReturn(member);
        willThrow(new CoreException(ErrorType.INVALID_VERIFICATION_PURPOSE))
                .given(memberValidator).validateUpdatePassword(any(), any());

        // when & then
        assertThatThrownBy(() -> memberService.updatePassword("newPassword123!", "signup.token", MemberFixture.DEFAULT_EMAIL))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_VERIFICATION_PURPOSE.getMessage());

        verifyNoInteractions(memberManager);
    }

}