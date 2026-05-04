package org.smu.randsome.randsomeback.domain.member.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.dto.CandidateIdMbti;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail_AddressAndStatus(String email, EntityStatus status);
    boolean existsByIdAndStatus(Long id, EntityStatus status);
    Optional<Member> findByEmail_AddressAndStatus(String email, EntityStatus status);
    Optional<Member> findByEmail_AddressAndStatusNot(String email, EntityStatus status);
    Optional<Member> findByIdAndStatus(Long memberId, EntityStatus status);
    Optional<Member> findByIdAndStatusNot(Long memberId, EntityStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.status = :status")
    Optional<Member> findByIdAndStatusWithLock(@Param("id") Long id, @Param("status") EntityStatus status);
    Optional<Member> findByRefreshTokenAndStatus(String refreshToken, EntityStatus status);

    @Query("""
                SELECT m.id FROM Member m
                WHERE m.gender = :gender
                  AND m.department <> :excludeDepartment
                  AND m.role = :role
                  AND m.status = :status
            """)
    List<Long> findCandidateIdsByGenderExcludingDepartment(
            @Param("gender") Gender gender,
            @Param("excludeDepartment") Department excludeDepartment,
            @Param("role") Role role,
            @Param("status") EntityStatus status
    );

    @Query("""
                SELECT m.id FROM Member m
                WHERE m.gender = :gender
                  AND m.role = :role
                  AND m.status = :status
            """)
    List<Long> findCandidateIdsByGender(
            @Param("gender") Gender gender,
            @Param("role") Role role,
            @Param("status") EntityStatus status
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

    @Query("""
                SELECT new org.smu.randsome.randsomeback.domain.member.dto.CandidateIdMbti(m.id, m.mbti)
                FROM Member m
                WHERE m.gender = :gender
                  AND m.department <> :excludeDepartment
                  AND m.role = :role
                  AND m.status = :status
            """)
    List<CandidateIdMbti> findCandidateIdAndMbtiByGenderExcludingDepartment(
            @Param("gender") Gender gender,
            @Param("excludeDepartment") Department excludeDepartment,
            @Param("role") Role role,
            @Param("status") EntityStatus status
    );

    @Query("""
                SELECT new org.smu.randsome.randsomeback.domain.member.dto.CandidateIdMbti(m.id, m.mbti)
                FROM Member m
                WHERE m.gender = :gender
                  AND m.role = :role
                  AND m.status = :status
            """)
    List<CandidateIdMbti> findCandidateIdAndMbtiByGender(
            @Param("gender") Gender gender,
            @Param("role") Role role,
            @Param("status") EntityStatus status
    );

}