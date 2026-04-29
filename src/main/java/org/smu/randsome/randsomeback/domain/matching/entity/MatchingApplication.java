package org.smu.randsome.randsomeback.domain.matching.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

/**
 * 매칭 신청을 나타내는 엔티티다.
 * <br/>회원이 매칭을 신청하면 이 엔티티를 통해 신청 정보가 관리되며, 상태는 라이프사이클을 따른다.
 * <br/>상태 전이: PENDING → SUCCESS / PARTIAL_MATCH / FAILED (또는) PENDING → CANCELLED
 * <br/>이상형 매칭인 경우 선호하는 태그들을 카테고리별 다중 선택으로 저장하여 매칭 필터링에 활용한다.
 */
@Table(indexes = {
        @Index(name = "idx_matching_app_created_status",
                columnList = "created_at, status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MatchingApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingType matchingType;

    @Column(nullable = false)
    private Integer applicationCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    @ElementCollection
    @CollectionTable(
            name = "matching_preferred_personality_tag",
            joinColumns = @JoinColumn(name = "matching_application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "personality_tag")
    private Set<PersonalityTag> preferredPersonalityTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "matching_preferred_face_type_tag",
            joinColumns = @JoinColumn(name = "matching_application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "face_type_tag")
    private Set<FaceTypeTag> preferredFaceTypeTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "matching_preferred_dating_style_tag",
            joinColumns = @JoinColumn(name = "matching_application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "dating_style_tag")
    private Set<DatingStyleTag> preferredDatingStyleTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "matching_preferred_mbti",
            joinColumns = @JoinColumn(name = "matching_application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "mbti")
    private Set<Mbti> preferredMbtis = new HashSet<>();

    @Version
    private Long version;

    private Integer matchedCount;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    /**
     * 이상형 조건 없이 매칭 신청을 생성한다 (주로 랜덤 매칭 용).
     */
    public static MatchingApplication apply(
            Member member,
            MatchingType matchingType,
            Integer applicationCount
    ) {
        return apply(member, matchingType, applicationCount, null);
    }

    /**
     * 이상형 조건을 포함하여 매칭 신청을 생성한다 (주로 이상형 매칭 용).
     */
    public static MatchingApplication apply(
            Member member,
            MatchingType matchingType,
            Integer applicationCount,
            IdealTypePreference idealTypePreference
    ) {
        validateApplicationCount(requireNonNull(applicationCount));

        MatchingApplication matchingApplication = new MatchingApplication();

        matchingApplication.member = requireNonNull(member);
        matchingApplication.matchingType = requireNonNull(matchingType);
        matchingApplication.applicationCount = applicationCount;
        matchingApplication.applicationStatus = ApplicationStatus.PENDING;
        matchingApplication.cancelledAt = null;
        matchingApplication.completedAt = null;
        matchingApplication.matchedCount = null;

        if (idealTypePreference != null) {
            matchingApplication.preferredPersonalityTags = new HashSet<>(idealTypePreference.preferredPersonalityTags());
            matchingApplication.preferredFaceTypeTags = new HashSet<>(idealTypePreference.preferredFaceTypeTags());
            matchingApplication.preferredDatingStyleTags = new HashSet<>(idealTypePreference.preferredDatingStyleTags());
            matchingApplication.preferredMbtis = new HashSet<>(idealTypePreference.preferredMbtis());
        }

        return matchingApplication;
    }

    /**
     * 이 신청에 저장된 이상형 조건을 Value Object로 반환한다.
     */
    public IdealTypePreference getIdealTypePreference() {
        return IdealTypePreference.of(
                preferredPersonalityTags,
                preferredFaceTypeTags,
                preferredDatingStyleTags,
                preferredMbtis
        );
    }

    public void complete(LocalDateTime completedAt, int matchedCount) {
        this.applicationStatus = resolveCompletionStatus(matchedCount);
        this.completedAt = requireNonNull(completedAt);
        this.matchedCount = matchedCount;
    }

    private ApplicationStatus resolveCompletionStatus(int matchedCount) {
        if (matchedCount <= 0) {
            return ApplicationStatus.FAILED;
        }
        if (matchedCount < this.applicationCount) {
            return ApplicationStatus.PARTIAL_MATCH;
        }
        return ApplicationStatus.SUCCESS;
    }

    public void cancel(LocalDateTime cancelledAt) {
        if (applicationStatus.isCompleted()) {
            throw new CoreException(ErrorType.NOT_ALLOW_CANCEL_APPROVED);
        }
        if (applicationStatus.equals(ApplicationStatus.CANCELLED)) {
            return;
        }

        this.applicationStatus = ApplicationStatus.CANCELLED;
        this.cancelledAt = requireNonNull(cancelledAt);
    }

    public Gender getTargetGender() {
        return this.getMember().getGender() == Gender.MALE ? Gender.FEMALE : Gender.MALE;
    }

    private static void validateApplicationCount(int applicationCount) {
        if (applicationCount < 1 || applicationCount > 5) {
            throw new CoreException(ErrorType.INVALID_PERSON_COUNT);
        }
    }

}
