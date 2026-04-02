package org.smu.randsome.randsomeback.domain.bankaccount.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.bankaccount.entity.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.implement.BankAccountReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BankAccountService {

    private final BankAccountReader bankAccountReader;

    @Transactional(readOnly = true)
    public BankAccount findByMemberId(Long memberId) {
        return bankAccountReader.findByMemberId(memberId);
    }

}