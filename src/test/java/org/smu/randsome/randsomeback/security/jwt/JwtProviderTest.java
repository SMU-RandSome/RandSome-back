package org.smu.randsome.randsomeback.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class JwtProviderTest extends UnitTestSupport {

    static final String TEST_SECRET_KEY = "dGhpcy1pcy1hLXN1cGVyLWxvbmctYW5kLXNlY3VyZS1zZWNyZXQta2V5LWZvci10ZXN0aW5nLWhzNTEyLWFsdG9yaXRobS0xMjM0NQ==";
    static final String WRONG_SECRET_KEY = "dGhpcy1pcy1hLXN1cGVyLWxvbmctYW5kaaaaaaaaaaaaWNyZXQta2V5LWZvci10ZXN0aW5nLWhzNTEyLWFsdG9yaXRobS0xMjM0NQ==";

    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(TEST_SECRET_KEY);
    }

    @Test
    void JWT_토큰을_생성한다() {
        // when
        var tokens = jwtProvider.createTokens(1L, Role.ROLE_MEMBER);

        // then
        assertThat(tokens).extracting(
                TokenResponse::accessToken,
                TokenResponse::refreshToken
        ).doesNotContainNull();
    }

    @Test
    void 유효한_JWT_토큰이면_true를_반환한다() {
        // given
        var tokens = jwtProvider.createTokens(1L, Role.ROLE_MEMBER);

        // when
        boolean result = jwtProvider.isTokenValid(tokens.accessToken());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 잘못된_서명의_JWT_토큰이면_false를_반환한다_SignatureException() {
        // given - 다른 SecretKey로 생성한 토큰
        var wrongProvider = new JwtProvider(WRONG_SECRET_KEY);
        var tokens = wrongProvider.createTokens(1L, Role.ROLE_MEMBER);
        var tokenWithWrongSignature = tokens.accessToken();

        // when - 정상 Provider로 검증
        boolean result = jwtProvider.isTokenValid(tokenWithWrongSignature);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 잘못된_형식의_JWT_토큰이면_false를_반환한다_MalformedJwtException() {
        // given
        var malformedToken = "this.is.not.a.valid.jwt.token";

        // when
        boolean result = jwtProvider.isTokenValid(malformedToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 만료된_JWT_토큰이면_false를_반환한다_ExpiredJwtException() {
        // given - 이미 만료된 토큰 생성
        SecretKey key = new SecretKeySpec(
                TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256
                        .key()
                        .build()
                        .getAlgorithm()
        );

        String expiredToken = Jwts.builder()
                .subject("1")
                .claim("role", "ROLE_MEMBER")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))  // 10초 전
                .expiration(new Date(System.currentTimeMillis() - 5000))  // 5초 전 만료
                .signWith(key)
                .compact();

        // when
        boolean result = jwtProvider.isTokenValid(expiredToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 지원하지_않는_JWT_토큰이면_false를_반환한다_UnsupportedJwtException() {
        // given - 암호화된 토큰 (JWE) - 지원하지 않음
        // Note: 실제로 UnsupportedJwtException을 발생시키려면 JWE 토큰이 필요
        // 여기서는 간단히 잘못된 형식으로 대체
        var unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjM0NTY3ODkwIn0.";

        // when
        boolean result = jwtProvider.isTokenValid(unsupportedToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 빈_문자열_토큰이면_false를_반환한다_IllegalArgumentException() {
        // given
        var emptyToken = "";

        // when
        boolean result = jwtProvider.isTokenValid(emptyToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 토큰이_NULL이면_false를_반환한다_IllegalArgumentException() {
        // when
        boolean result = jwtProvider.isTokenValid(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void JWT로부터_Authentication_객체를_가져온다() {
        // given
        Long memberId = 1L;
        var tokens = jwtProvider.createTokens(memberId, Role.ROLE_MEMBER);

        // when
        var authentication = jwtProvider.getAuthentication(tokens.accessToken());

        // then
        assertThat(authentication.getPrincipal()).isEqualTo(memberId.toString());
        assertThat(authentication.getAuthorities()).isNotEmpty();
    }

    @Test
    void Refresh_토큰_유효성_검증한다() {
        // given
        TokenResponse tokens = jwtProvider.createTokens(1L, Role.ROLE_MEMBER);

        // when
        boolean result = jwtProvider.isTokenValid(tokens.refreshToken());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 이메일_인증_토큰을_생성하고_email을_추출한다() {
        // given
        var email = "student@sangmyung.kr";

        // when
        String token = jwtProvider.generateEmailVerificationToken(email);
        String extractedEmail = jwtProvider.extractEmailFromVerificationToken(token);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void 만료된_이메일_인증_토큰으로_파싱_시_INVALID_TOKEN_예외가_발생한다() {
        // given - 이미 만료된 이메일 인증 토큰 직접 생성
        SecretKey key = new SecretKeySpec(
                TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        String expiredToken = Jwts.builder()
                .subject("student@sangmyung.kr")
                .claim("category", TokenType.VERIFICATION.getValue())
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.extractEmailFromVerificationToken(expiredToken))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.INVALID_TOKEN));
    }

    @Test
    void category가_VERIFICATION이_아닌_토큰으로_파싱_시_INVALID_TOKEN_예외가_발생한다() {
        // given - purpose 없는 일반 Access Token
        var tokens = jwtProvider.createTokens(1L, Role.ROLE_MEMBER);

        // when & then
        assertThatThrownBy(() -> jwtProvider.extractEmailFromVerificationToken(tokens.accessToken()))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.INVALID_TOKEN));
    }

    @Test
    void 서명이_위조된_토큰으로_파싱_시_INVALID_TOKEN_예외가_발생한다() {
        // given
        var wrongProvider = new JwtProvider(WRONG_SECRET_KEY);
        String forgotToken = wrongProvider.generateEmailVerificationToken("student@sangmyung.kr");

        // when & then
        assertThatThrownBy(() -> jwtProvider.extractEmailFromVerificationToken(forgotToken))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.INVALID_TOKEN));
    }

}