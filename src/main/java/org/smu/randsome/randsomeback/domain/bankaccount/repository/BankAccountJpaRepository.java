package org.smu.randsome.randsomeback.domain.bankaccount.repository;

import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountJpaRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByMemberId(Long memberId);

}