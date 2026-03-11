package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByIdAndStatus(Long memberId, EntityStatus status);
    Page<Member> findAllByStatus(EntityStatus status, Pageable pageable);

}