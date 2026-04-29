package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.auth.enums.VerificationPurpose;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberValidatorTest extends UnitTestSupport {

    @InjectMocks
    MemberValidator memberValidator;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void 이메일이_일치하면_회원가입_토큰_검증에_성공한다() {
        // given
        String token = "signup.verification.token";
        String email = "student@sangmyung.kr";
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.SIGN_UP);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn(email);

        // when
        memberValidator.validateSignUpToken(token, email);

        // then
        verify(jwtProvider).extractVerificationPurposeFromToken(token);
        verify(jwtProvider).extractEmailFromVerificationToken(token);
    }

    @Test
    void 이메일이_불일치하면_회원가입_요청_예외가_발생한다() {
        // given
        String token = "signup.verification.token";
        String requestEmail = "request@sangmyung.kr";
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.SIGN_UP);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn("token@sangmyung.kr");

        // when & then
        assertThatThrownBy(() -> memberValidator.validateSignUpToken(token, requestEmail))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_SIGNUP_REQUEST.getMessage());
    }

    @Test
    void 토큰에서_이메일_추출_실패_예외는_그대로_전파된다() {
        // given
        String token = "invalid.token";
        String requestEmail = "request@sangmyung.kr";
        CoreException invalidTokenException = new CoreException(ErrorType.INVALID_TOKEN);
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.SIGN_UP);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willThrow(invalidTokenException);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateSignUpToken(token, requestEmail))
                .isSameAs(invalidTokenException);
    }

    @Test
    void 회원가입_토큰의_목적이_다르면_예외가_발생하고_이메일_검증을_진행하지_않는다() {
        // given
        String token = "password.verification.token";
        String requestEmail = "request@sangmyung.kr";
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.PASSWORD_RESET);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateSignUpToken(token, requestEmail))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_VERIFICATION_PURPOSE.getMessage());

        verify(jwtProvider).extractVerificationPurposeFromToken(token);
        verifyNoMoreInteractions(jwtProvider);
    }

    @Test
    void 이메일이_일치하면_비밀번호_수정_토큰_검증에_성공한다() {
        // given
        String token = "password.verification.token";
        Member member = mock(Member.class);
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.PASSWORD_RESET);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn("student@sangmyung.kr");
        given(member.isEmailCorrect("student@sangmyung.kr")).willReturn(true);

        // when
        memberValidator.validateUpdatePassword(token, member);

        // then
        verify(jwtProvider).extractVerificationPurposeFromToken(token);
        verify(jwtProvider).extractEmailFromVerificationToken(token);
    }

    @Test
    void 이메일이_불일치하면_비밀번호_수정_요청_예외가_발생한다() {
        // given
        String token = "password.verification.token";
        Member member = mock(Member.class);
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.PASSWORD_RESET);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn("other@sangmyung.kr");
        given(member.isEmailCorrect("other@sangmyung.kr")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateUpdatePassword(token, member))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST.getMessage());
    }

    @Test
    void 비밀번호_수정_토큰에서_이메일_추출_실패_예외는_그대로_전파된다() {
        // given
        String token = "invalid.token";
        Member member = mock(Member.class);
        CoreException invalidTokenException = new CoreException(ErrorType.INVALID_TOKEN);
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.PASSWORD_RESET);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willThrow(invalidTokenException);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateUpdatePassword(token, member))
                .isSameAs(invalidTokenException);
    }

    @Test
    void 일반_회원이_올바른_비밀번호로_탈퇴_검증하면_통과한다() {
        // given
        Member member = mock(Member.class);
        given(member.isAdmin()).willReturn(false);
        given(member.isPasswordCorrect("password123!", passwordEncoder)).willReturn(true);

        // when & then
        assertThatCode(() -> memberValidator.validateWithdraw(member, "password123!"))
                .doesNotThrowAnyException();
    }

    @Test
    void 관리자가_탈퇴_검증하면_ADMIN_CANNOT_WITHDRAW_예외가_발생한다() {
        // given
        Member member = mock(Member.class);
        given(member.isAdmin()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateWithdraw(member, "password123!"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ADMIN_CANNOT_WITHDRAW.getMessage());
    }

    @Test
    void 잘못된_비밀번호로_탈퇴_검증하면_INCORRECT_PASSWORD_예외가_발생한다() {
        // given
        Member member = mock(Member.class);
        given(member.isAdmin()).willReturn(false);
        given(member.isPasswordCorrect("wrongPassword!", passwordEncoder)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateWithdraw(member, "wrongPassword!"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INCORRECT_PASSWORD.getMessage());
    }

    @Test
    void 비밀번호_수정_토큰의_목적이_다르면_예외가_발생하고_이메일_검증을_진행하지_않는다() {
        // given
        String token = "signup.verification.token";
        Member member = mock(Member.class);
        given(jwtProvider.extractVerificationPurposeFromToken(token)).willReturn(VerificationPurpose.SIGN_UP);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateUpdatePassword(token, member))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_VERIFICATION_PURPOSE.getMessage());

        verify(jwtProvider).extractVerificationPurposeFromToken(token);
        verifyNoMoreInteractions(jwtProvider);
    }

}