package org.smu.randsome.randsomeback.global.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenHasher {

    private static final String ALGORITHM = "SHA-256";

    /**
     * RefreshToken을 SHA-256으로 해시화
     * DB 저장 시 사용
     */
    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            // NOTE: SHA-256 알고리즘은  모든 JVM 에서 필수 지원하는 표준 알고리즘이다. 항상 존재하므로 이 예외는 발생하지 않아야 합니다.
            // 만약 발생한다면 심각한 문제이므로 런타임 예외로 감싸서 던집니다.
            throw new IllegalStateException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}