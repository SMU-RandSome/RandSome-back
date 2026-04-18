package org.smu.randsome.randsomeback.domain.member.repository;

import org.smu.randsome.randsomeback.domain.member.entity.MemberRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRestrictionJpaRepository extends JpaRepository<MemberRestriction, Long> {

}