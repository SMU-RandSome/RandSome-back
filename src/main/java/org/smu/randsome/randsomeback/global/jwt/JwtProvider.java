package org.smu.randsome.randsomeback.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenExpiration;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private static final String CATEGORY_KEY = "category";

    public JwtProvider(@Value("${spring.jwt.secretKey}") String key) {
        this.secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256
                        .key()
                        .build()
                        .getAlgorithm()
        );
    }

    public TokenResponse createTokens(Long memberId, Role role) {
        String accessToken = createAccessToken(memberId, role);
        String refreshToken = createRefreshToken();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);

        String memberId = claims.getSubject();
        String role = getAuthority(claims).name();
        GrantedAuthority authority = new SimpleGrantedAuthority(role);

        return new UsernamePasswordAuthenticationToken(
                memberId,
                null,
                Collections.singletonList(authority)
        );
    }

    public boolean isTokenValid(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SignatureException e) {
            log.warn("[Invalid JWT signature], 유효하지 않는 JWT 서명 입니다. Token prefix: {}", maskToken(token));
        } catch (MalformedJwtException e) {
            log.warn("[Invalid JWT malformed], 잘못된 형식의 JWT 입니다. Token prefix: {}", maskToken(token));
        } catch (ExpiredJwtException e) {
            log.info("[Expired JWT], 만료된 JWT 입니다. Token prefix: {}", maskToken(token));
        } catch (UnsupportedJwtException e) {
            log.warn("[Unsupported JWT], 지원되지 않는 JWT 입니다. Token prefix: {}", maskToken(token));
        } catch (IllegalArgumentException e) {
            log.warn("[JWT claims is empty], 잘못된 JWT 입니다. Token prefix: {}", maskToken(token));
        }
        return false;
    }

    private String createAccessToken(Long memberId, Role role) {
        Date validity = getTokenExpirationTime(TokenExpiration.ACCESS_TOKEN);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CATEGORY_KEY, TokenType.ACCESS.getValue())
                .claim(TokenType.AUTHORIZATION_HEADER.getValue(), role)
                .issuedAt(new Date())
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    private Date getTokenExpirationTime(TokenExpiration expiration) {
        Date date = new Date();
        return new Date(date.getTime() + expiration.getExpirationTime());
    }

    private String createRefreshToken() {
        Date validity = getTokenExpirationTime(TokenExpiration.REFRESH_TOKEN);

        return Jwts.builder()
                .claim(CATEGORY_KEY, TokenType.REFRESH.getValue())
                .issuedAt(new Date())
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    private Role getAuthority(Claims claims) {
        return Role.valueOf(claims.get(TokenType.AUTHORIZATION_HEADER.getValue(), String.class));
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 5);
    }

}