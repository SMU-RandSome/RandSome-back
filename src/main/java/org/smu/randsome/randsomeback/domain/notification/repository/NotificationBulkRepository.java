package org.smu.randsome.randsomeback.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NotificationBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(List<Long> memberIds, NotificationType type, LocalDateTime now) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO notification (member_id, type, title, body, status, created_at, updated_at) VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?)",
                memberIds,
                500,
                (ps, memberId) -> {
                    ps.setLong(1, memberId);
                    ps.setString(2, type.name());
                    ps.setString(3, type.getTitle());
                    ps.setString(4, type.getMessage());
                    ps.setObject(5, now);
                    ps.setObject(6, now);
                }
        );
        log.info("[NotificationBulkRepository] {}개의 알림이 데이터베이스에 저장되었습니다.", memberIds.size());
    }

}