package org.smu.randsome.randsomeback.domain.bankaccount.dto.command;

import lombok.Builder;

@Builder
public record UpdateBankAccount(
        String bankName,
        String accountNumber,
        String accountHolder
) {

}