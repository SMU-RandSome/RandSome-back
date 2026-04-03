package org.smu.randsome.randsomeback.domain.member.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByIdAndStatus(Long memberId, EntityStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.status = :status")
    Optional<Member> findByIdAndStatusWithLock(@Param("id") Long id, @Param("status") EntityStatus status);
    Optional<Member> findByRefreshTokenAndStatus(String refreshToken, EntityStatus status);
    Page<Member> findAllByStatusAndRoleNot(EntityStatus status, Role role, Pageable pageable);

    @Query(value = """
            SELECT * FROM member
            WHERE gender = :gender
              AND role = 'ROLE_CANDIDATE'
              AND status = 'ACTIVE'
            ORDER BY RAND()
            LIMIT :count
            """, nativeQuery = true)
    List<Member> findRandomCandidatesByGender(
            @Param("gender") String gender,
            @Param("count") int count
    );

    @Query("""
                SELECT m.gender, COUNT(m)
                FROM Member m
                WHERE m.role = :role
                  AND m.status = :status
                GROUP BY m.gender
            """
    )
    List<CandidateGenderCountItem> findAllGenderCountBy(
            @Param("role") Role role,
            @Param("status") EntityStatus status
    );

}