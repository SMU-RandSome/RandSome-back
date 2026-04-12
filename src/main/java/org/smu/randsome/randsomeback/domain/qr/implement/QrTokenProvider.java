package org.smu.randsome.randsomeback.domain.qr.implement;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenExpiration;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * QR 코드에 담길 단기 JWT를 생성하고 파싱하는 컴포넌트.
 *
 * <p>생성된 토큰의 클레임 구조:
 * <ul>
 *   <li>{@code sub} - 회원 ID</li>
 *   <li>{@code jti} - 토큰 고유 식별자 (UUID, 일회용 추적에 사용)</li>
 *   <li>{@code category} - {@code "qrToken"} 고정값 (다른 JWT 토큰과 구분)</li>
 *   <li>{@code exp} - 발급 시각 + 30초</li>
 * </ul>
 *
 * <p>서명 알고리즘: HS256
 */
@Slf4j
@Component
public class QrTokenProvider {

    private static final String CATEGORY_KEY = "category";

    private final SecretKey secretKey;

    public QrTokenProvider(@Value("${spring.jwt.secretKey}") String key) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * {@link #createToken(Long)} 의 반환값.
     *
     * @param token JWT 문자열
     * @param jti   토큰 고유 식별자
     */
    public record CreatedQrToken(String token, String jti) {
    }

    /**
     * {@link #parse(String)} 의 반환값.
     *
     * @param memberId 회원 ID
     * @param jti      토큰 고유 식별자
     */
    public record ParsedQrToken(Long memberId, String jti) {
    }

    /**
     * 회원 ID를 담은 30초 만료 QR 토큰을 생성한다.
     *
     * @param memberId 토큰에 포함할 회원 ID
     * @return 생성된 JWT 문자열과 jti를 담은 {@link CreatedQrToken}
     */
    public CreatedQrToken createToken(Long memberId) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(TokenExpiration.QR_TOKEN.getExpirationTime());
        String token = Jwts.builder()
                .subject(String.valueOf(memberId))
                .id(jti)
                .claim(CATEGORY_KEY, TokenType.QR.getValue())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
        return new CreatedQrToken(token, jti);
    }

    /**
     * QR 토큰을 파싱하여 회원 ID와 jti를 추출한다.
     *
     * @param token 검증할 JWT 문자열
     * @return 파싱된 회원 ID와 jti를 담은 {@link ParsedQrToken}
     * @throws CoreException {@link ErrorType#QR_TOKEN_EXPIRED} - 토큰이 만료된 경우
     * @throws CoreException {@link ErrorType#INVALID_QR_TOKEN} - 서명 불일치, 형식 오류, 잘못된 category인 경우
     */
    public ParsedQrToken parse(String token) {
        Claims claims = parseClaims(token);
        return new ParsedQrToken(Long.parseLong(claims.getSubject()), claims.getId());
    }

    private Claims parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!TokenType.QR.getValue().equals(claims.get(CATEGORY_KEY, String.class))) {
                throw new CoreException(ErrorType.INVALID_QR_TOKEN);
            }

            return claims;
        } catch (ExpiredJwtException e) {
            log.info("[QR Token Expired] token prefix: {}", maskToken(token));
            throw new CoreException(ErrorType.QR_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[QR Token Invalid] token prefix: {}", maskToken(token));
            throw new CoreException(ErrorType.INVALID_QR_TOKEN);
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 5);
    }

}