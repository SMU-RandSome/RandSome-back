package org.smu.randsome.randsomeback.domain.bankaccount.repository;

import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountJpaRepository extends JpaRepository<BankAccount, Long> {

}