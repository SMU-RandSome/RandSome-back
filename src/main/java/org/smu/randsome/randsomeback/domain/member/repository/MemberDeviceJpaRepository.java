package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberDeviceJpaRepository extends JpaRepository<MemberDevice, Long> {

    Optional<MemberDevice> findByMemberIdAndDeviceTokenAndStatus(Long memberId, String deviceToken, EntityStatus status);

    Optional<MemberDevice> findByMemberIdAndDeviceToken(Long memberId, String deviceToken);

    List<MemberDevice> findAllByStatus(EntityStatus status);

    @Query("SELECT m FROM MemberDevice m WHERE m.member.role = :role AND m.status = :status")
    List<MemberDevice> findAllByAdmin(@Param("role") Role role, @Param("status") EntityStatus status);

    List<MemberDevice> findByMemberIdAndStatus(Long memberId, EntityStatus status);
}