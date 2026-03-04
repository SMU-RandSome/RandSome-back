package org.smu.randsome.randsomeback.domain.member.repository;

import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

}