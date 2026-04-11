package org.smu.randsome.randsomeback.domain.member.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MemberRestriction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private String reason;

    // NOTE: 현재는 영구정지이기 떄문에 시간 관련 필드는 추가하지 않음
    public static MemberRestriction create(Member member, String reason) {
        MemberRestriction memberRestriction = new MemberRestriction();

        memberRestriction.member = requireNonNull(member);
        memberRestriction.reason = requireNonNull(reason);

        return memberRestriction;
    }

}