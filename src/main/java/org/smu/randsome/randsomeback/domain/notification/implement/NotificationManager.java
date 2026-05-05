package org.smu.randsome.randsomeback.domain.notification.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.notification.repository.NotificationBulkRepository;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationManager {

    private final NotificationBulkRepository notificationBulkRepository;

    // NOTE: JDBC batchUpdate를 활용하기에 트랜잭션을 적용하여 일괄 저장 처리
    @Transactional
    public void saveNotifications(List<Long> memberIds, NotificationType type) {
        notificationBulkRepository.saveAll(memberIds, type, LocalDateTime.now());
    }

}