package org.smu.randsome.randsomeback.domain.bankaccount.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.bankaccount.dto.command.BankAccountInfo;
import org.smu.randsome.randsomeback.domain.bankaccount.entity.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.repository.BankAccountJpaRepository;
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