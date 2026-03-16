package org.smu.randsome.randsomeback.fixture;

import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;

public class BankAccountFixture {

    public static final Long DEFAULT_MEMBER_ID = 1L;
    public static final String DEFAULT_BANK_NAME = "KB국민은행";
    public static final String DEFAULT_ACCOUNT_NUMBER = "111-222-333333";
    public static final String DEFAULT_ACCOUNT_HOLDER = "홍길동";

    public static BankAccount create() {
        return BankAccount.register(
                DEFAULT_MEMBER_ID,
                DEFAULT_BANK_NAME,
                DEFAULT_ACCOUNT_NUMBER,
                DEFAULT_ACCOUNT_HOLDER
        );
    }

    public static BankAccount createWithCustomMemberId(Long memberId) {
        return BankAccount.register(
                memberId,
                DEFAULT_BANK_NAME,
                DEFAULT_ACCOUNT_NUMBER,
                DEFAULT_ACCOUNT_HOLDER
        );
    }

    public static BankAccount createCustom(
            Long memberId,
            String bankName,
            String accountNumber,
            String accountHolder
    ) {
        return BankAccount.register(memberId, bankName, accountNumber, accountHolder);
    }

}