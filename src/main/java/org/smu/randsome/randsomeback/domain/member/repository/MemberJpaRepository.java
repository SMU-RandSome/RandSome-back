package org.smu.randsome.randsomeback.domain.member.repository;

import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByIdAndStatus(Long memberId, EntityStatus status);
    Page<Member> findAllByStatusAndRoleNot(EntityStatus status, Role role, Pageable pageable);

}