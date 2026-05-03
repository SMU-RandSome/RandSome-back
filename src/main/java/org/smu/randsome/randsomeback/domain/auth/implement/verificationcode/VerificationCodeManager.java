package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.security.SecureRandom;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerificationCodeManager {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final Duration SEND_COOLDOWN = Duration.ofSeconds(60);

    private final VerificationCodeStore codeStore;
    private final RedisRepository redisRepository;

    public String generateVerificationCode(String email) {
        if (!redisRepository.tryAcquire(CacheKeys.verificationCodeSendCooldown(email), SEND_COOLDOWN)) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_SEND_COOLDOWN);
        }

        String code = generateCode();
        codeStore.put(email, code);

        return code;
    }

    public void invalidateVerificationCode(String email) {
        codeStore.remove(email);
    }

    private static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(SECURE_RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

}