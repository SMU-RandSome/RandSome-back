package org.smu.randsome.randsomeback.domain.bankaccount.service.command;

public record BankAccountInfo(
        String bankName,
        String accountNumber,
        String accountHolder
) {

}