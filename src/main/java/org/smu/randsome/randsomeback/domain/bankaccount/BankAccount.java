package org.smu.randsome.randsomeback.domain.bankaccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BankAccount extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountHolder;

    public static BankAccount register(
            Long memberId,
            String bankName,
            String accountNumber,
            String accountHolder
    ) {
        BankAccount bankAccount = new BankAccount();

        bankAccount.memberId = memberId;
        bankAccount.bankName = bankName;
        bankAccount.accountNumber = accountNumber;
        bankAccount.accountHolder = accountHolder;

        return bankAccount;
    }

}