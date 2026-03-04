package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class VerificationCodeStore {

    private final Clock clock;
    private final Map<String, VerificationCodeEntry> store = new ConcurrentHashMap<>();

    void put(String email, VerificationCodeEntry entry) {
        store.put(email, entry);
    }

    VerificationCodeEntry get(String email) {
        return store.get(email);
    }

    void remove(String email) {
        store.remove(email);
    }

    boolean removeIfPresent(String email, VerificationCodeEntry entry) {
        return store.remove(email, entry);
    }

    // NOTE: 로직을 옮겨야할까?
    @Scheduled(fixedRate = 60_000)
    void removeExpiredEntries() {
        store.entrySet().removeIf(e -> e.getValue().isExpired(clock));
    }

}