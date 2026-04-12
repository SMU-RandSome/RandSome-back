package org.smu.randsome.randsomeback.domain.qr.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider.CreatedQrToken;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider.ParsedQrToken;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class QrTokenProviderUnitTest extends UnitTestSupport {

    private static final String SECRET_KEY = "vmfhaltmskdlstkfkdgodyroqkfwkdbalroqkfwkdbalaaaaaaaaaaaaaaaabbbbb";

    private QrTokenProvider qrTokenProvider;

    @BeforeEach
    void setUp() {
        qrTokenProvider = new QrTokenProvider(SECRET_KEY);
    }

    @Test
    void 토큰_생성_시_memberId와_jti를_포함한_토큰이_반환된다() {
        // given
        Long memberId = 1L;

        // when
        CreatedQrToken created = qrTokenProvider.createToken(memberId);

        // then
        assertThat(created.token()).isNotBlank();
        assertThat(created.jti()).isNotBlank();
    }

    @Test
    void 같은_회원이_두_번_발급하면_서로_다른_jti가_생성된다() {
        // when
        CreatedQrToken first = qrTokenProvider.createToken(1L);
        CreatedQrToken second = qrTokenProvider.createToken(1L);

        // then
        assertThat(first.jti()).isNotEqualTo(second.jti());
    }

    @Test
    void 생성된_토큰을_파싱하면_올바른_memberId와_jti가_반환된다() {
        // given
        Long memberId = 42L;
        CreatedQrToken created = qrTokenProvider.createToken(memberId);

        // when
        ParsedQrToken parsed = qrTokenProvider.parse(created.token());

        // then
        assertThat(parsed.memberId()).isEqualTo(memberId);
        assertThat(parsed.jti()).isEqualTo(created.jti());
    }

    @Test
    void 만료된_토큰_파싱_시_QR_TOKEN_EXPIRED_예외가_발생한다() {
        // given
        String expiredToken = createExpiredToken(1L);

        // when & then
        assertThatThrownBy(() -> qrTokenProvider.parse(expiredToken))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.QR_TOKEN_EXPIRED.getMessage());
    }

    @Test
    void 잘못된_서명의_토큰_파싱_시_INVALID_QR_TOKEN_예외가_발생한다() {
        // given
        String wrongKeyToken = createTokenWithWrongKey(1L);

        // when & then
        assertThatThrownBy(() -> qrTokenProvider.parse(wrongKeyToken))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_QR_TOKEN.getMessage());
    }

    @Test
    void 형식이_잘못된_문자열_파싱_시_INVALID_QR_TOKEN_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> qrTokenProvider.parse("not.a.valid.jwt"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_QR_TOKEN.getMessage());
    }

    @Test
    void 다른_category_토큰_파싱_시_INVALID_QR_TOKEN_예외가_발생한다() {
        // given - accessToken category로 생성된 토큰
        String accessToken = createTokenWithCategory(1L, "accessToken");

        // when & then
        assertThatThrownBy(() -> qrTokenProvider.parse(accessToken))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_QR_TOKEN.getMessage());
    }

    // --- helpers ---

    private String createExpiredToken(Long memberId) {
        SecretKey key = buildSecretKey(SECRET_KEY);
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .id("expired-jti")
                .claim("category", "qrToken")
                .issuedAt(toDate(past.minusSeconds(30)))
                .expiration(toDate(past))
                .signWith(key)
                .compact();
    }

    private String createTokenWithWrongKey(Long memberId) {
        SecretKey wrongKey = buildSecretKey("wrongkeywrongkeywrongkeywrongkeywrongkeywrongkeywrongkeyaaaaabbbbb");

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .id("some-jti")
                .claim("category", "qrToken")
                .issuedAt(new Date())
                .expiration(toDate(LocalDateTime.now().plusSeconds(30)))
                .signWith(wrongKey)
                .compact();
    }

    private String createTokenWithCategory(Long memberId, String category) {
        SecretKey key = buildSecretKey(SECRET_KEY);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .id("some-jti")
                .claim("category", category)
                .issuedAt(new Date())
                .expiration(toDate(LocalDateTime.now().plusSeconds(30)))
                .signWith(key)
                .compact();
    }

    private SecretKey buildSecretKey(String raw) {
        return new SecretKeySpec(
                raw.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}