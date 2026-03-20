package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Email;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class MemberValidatorTest extends UnitTestSupport {

    @InjectMocks
    MemberValidator memberValidator;

    @Mock
    JwtProvider jwtProvider;

    @Test
    void 이메일이_일치하면_회원가입_토큰_검증에_성공한다() {
        // given
        String token = "signup.verification.token";
        String email = "student@sangmyung.kr";
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn(email);

        // when
        memberValidator.validateSignUpToken(token, email);

        // then
        verify(jwtProvider).extractEmailFromVerificationToken(token);
    }

    @Test
    void 이메일이_불일치하면_회원가입_요청_예외가_발생한다() {
        // given
        String token = "signup.verification.token";
        String requestEmail = "request@sangmyung.kr";
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
        given(jwtProvider.extractEmailFromVerificationToken(token)).willThrow(invalidTokenException);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateSignUpToken(token, requestEmail))
                .isSameAs(invalidTokenException);
    }

    @Test
    void 이메일이_일치하면_비밀번호_수정_토큰_검증에_성공한다() {
        // given
        String token = "password.verification.token";
        Email email = new Email("student@sangmyung.kr");
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn("student@sangmyung.kr");

        // when
        memberValidator.validateUpdatePassword(token, email);

        // then
        verify(jwtProvider).extractEmailFromVerificationToken(token);
    }

    @Test
    void 이메일이_불일치하면_비밀번호_수정_요청_예외가_발생한다() {
        // given
        String token = "password.verification.token";
        Email email = new Email("request@sangmyung.kr");
        given(jwtProvider.extractEmailFromVerificationToken(token)).willReturn("other@sangmyung.kr");

        // when & then
        assertThatThrownBy(() -> memberValidator.validateUpdatePassword(token, email))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST.getMessage());
    }

    @Test
    void 비밀번호_수정_토큰에서_이메일_추출_실패_예외는_그대로_전파된다() {
        // given
        String token = "invalid.token";
        Email email = new Email("student@sangmyung.kr");
        CoreException invalidTokenException = new CoreException(ErrorType.INVALID_TOKEN);
        given(jwtProvider.extractEmailFromVerificationToken(token)).willThrow(invalidTokenException);

        // when & then
        assertThatThrownBy(() -> memberValidator.validateUpdatePassword(token, email))
                .isSameAs(invalidTokenException);
    }

}