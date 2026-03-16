package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByIdAndStatus(Long memberId, EntityStatus status);
    Optional<Member> findByRefreshTokenAndStatus(String refreshToken, EntityStatus status);
    Page<Member> findAllByStatusAndRoleNot(EntityStatus status, Role role, Pageable pageable);
    List<Member> findAllByGenderAndRoleAndStatus(Gender gender, Role role, EntityStatus status);

}