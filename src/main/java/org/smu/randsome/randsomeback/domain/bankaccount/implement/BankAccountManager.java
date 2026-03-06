package org.smu.randsome.randsomeback.domain.bankaccount.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.repository.BankAccountJpaRepository;
import org.smu.randsome.randsomeback.domain.bankaccount.service.command.BankAccountInfo;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BankAccountManager {

    private final BankAccountJpaRepository bankAccountJpaRepository;

    public void create(Long memberId, BankAccountInfo bankAccountInfo) {
        bankAccountJpaRepository.save(BankAccount.register(
                memberId,
                bankAccountInfo.bankName(),
                bankAccountInfo.accountNumber(),
                bankAccountInfo.accountHolder()
        ));
    }

}