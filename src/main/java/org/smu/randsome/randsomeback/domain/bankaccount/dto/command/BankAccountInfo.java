package org.smu.randsome.randsomeback.domain.bankaccount.dto.command;

public record BankAccountInfo(
        String bankName,
        String accountNumber,
        String accountHolder
) {

}