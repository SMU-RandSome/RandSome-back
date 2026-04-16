package org.smu.randsome.randsomeback.domain.notification.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.notification.entity.Notification;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    // test 검증용 메서드
    List<Notification> findAllByMemberIdAndStatus(Long memberId, EntityStatus status);
}