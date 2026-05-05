package org.smu.randsome.randsomeback.domain.notification.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    public static Notification create(
            Long memberId,
            NotificationType type
    ) {
        Notification notification = new Notification();

        notification.memberId = requireNonNull(memberId);
        notification.type = requireNonNull(type);
        notification.title = type.getTitle();
        notification.body = type.getMessage();

        return notification;
    }

}