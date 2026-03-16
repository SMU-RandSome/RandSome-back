package org.smu.randsome.randsomeback.domain.bankaccount.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.repository.BankAccountJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BankAccountReader {

    private final BankAccountJpaRepository bankAccountJpaRepository;

    public BankAccount findByMemberId(Long memberId) {
        return bankAccountJpaRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_BANK_ACCOUNT));
    }

}
