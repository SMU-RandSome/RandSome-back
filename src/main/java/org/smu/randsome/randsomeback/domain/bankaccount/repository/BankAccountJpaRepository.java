package org.smu.randsome.randsomeback.domain.bankaccount.repository;

import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountJpaRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByMemberIdAndStatus(Long memberId, EntityStatus status);

}