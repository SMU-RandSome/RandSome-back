package org.smu.randsome.randsomeback.domain.matching.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

/**
 * 이상형 매칭 신청 시 선택한 선호 태그 스냅샷을 나타내는 엔티티다.
 * <br/>매칭 신청 당시의 이상형 조건을 독립적으로 기록하며, MatchingApplication과 단방향 참조 관계를 가진다.
 * <br/>향후 태그별 통계 쿼리(선호 태그 분포, 인기 태그 등)의 기반 테이블로 활용한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MatchingIdealTypeSnapshot extends BaseEntity {

    @Column(name = "matching_application_id", nullable = false, unique = true)
    private Long matchingApplicationId;

    @ElementCollection
    @CollectionTable(
            name = "ideal_type_snapshot_preferred_personality_tag",
            joinColumns = @JoinColumn(name = "matching_ideal_type_snapshot_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "personality_tag", nullable = false)
    private Set<PersonalityTag> preferredPersonalityTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "ideal_type_snapshot_preferred_face_type_tag",
            joinColumns = @JoinColumn(name = "matching_ideal_type_snapshot_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "face_type_tag", nullable = false)
    private Set<FaceTypeTag> preferredFaceTypeTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "ideal_type_snapshot_preferred_dating_style_tag",
            joinColumns = @JoinColumn(name = "matching_ideal_type_snapshot_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "dating_style_tag", nullable = false)
    private Set<DatingStyleTag> preferredDatingStyleTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "ideal_type_snapshot_preferred_mbti",
            joinColumns = @JoinColumn(name = "matching_ideal_type_snapshot_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", nullable = false)
    private Set<Mbti> preferredMbtis = new HashSet<>();

    public static MatchingIdealTypeSnapshot create(
            Long matchingApplicationId,
            IdealTypePreference preference
    ) {
        requireNonNull(preference);

        MatchingIdealTypeSnapshot snapshot = new MatchingIdealTypeSnapshot();
        snapshot.matchingApplicationId = requireNonNull(matchingApplicationId);
        snapshot.preferredPersonalityTags = new HashSet<>(preference.preferredPersonalityTags());
        snapshot.preferredFaceTypeTags = new HashSet<>(preference.preferredFaceTypeTags());
        snapshot.preferredDatingStyleTags = new HashSet<>(preference.preferredDatingStyleTags());
        snapshot.preferredMbtis = new HashSet<>(preference.preferredMbtis());
        return snapshot;
    }

    public IdealTypePreference toVO() {
        return IdealTypePreference.of(
                preferredPersonalityTags,
                preferredFaceTypeTags,
                preferredDatingStyleTags,
                preferredMbtis
        );
    }

}
