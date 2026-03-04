package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class VerificationCodeStore {

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

}