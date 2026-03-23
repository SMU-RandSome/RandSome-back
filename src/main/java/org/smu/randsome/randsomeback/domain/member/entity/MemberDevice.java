package org.smu.randsome.randsomeback.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MemberDevice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false, length = 512)
    private String deviceToken;

    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    public static MemberDevice register(Member member, String deviceToken, LocalDateTime now) {
        MemberDevice memberDevice = new MemberDevice();

        memberDevice.member = member;
        memberDevice.deviceToken = deviceToken;
        memberDevice.lastSyncedAt = now;

        return memberDevice;
    }

    public void updateLastSyncedAt(LocalDateTime now) {
        this.lastSyncedAt = now;
    }

}