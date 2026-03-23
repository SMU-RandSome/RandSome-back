package org.smu.randsome.randsomeback.domain.notification.repository;

import org.smu.randsome.randsomeback.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

}