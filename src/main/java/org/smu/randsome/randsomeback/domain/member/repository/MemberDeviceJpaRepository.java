package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDeviceJpaRepository extends JpaRepository<MemberDevice, Long> {

    Optional<MemberDevice> findByMemberIdAndDeviceTokenAndStatus(Long memberId, String deviceToken, EntityStatus status);

    List<MemberDevice> findAllByStatus(EntityStatus status);
}