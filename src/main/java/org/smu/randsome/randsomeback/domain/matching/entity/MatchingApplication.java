package org.smu.randsome.randsomeback.domain.matching.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
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
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_personality_tag")
    private PersonalityTag preferredPersonalityTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_face_type_tag")
    private FaceTypeTag preferredFaceTypeTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_dating_style_tag")
    private DatingStyleTag preferredDatingStyleTag;

    @Version
    private Long version;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    public static MatchingApplication apply(
            Member member,
            MatchingType matchingType,
            Integer applicationCount
    ) {
        return apply(member, matchingType, applicationCount, null);
    }

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

        if (idealTypePreference != null) {
            matchingApplication.preferredPersonalityTag = idealTypePreference.preferredPersonalityTag();
            matchingApplication.preferredFaceTypeTag = idealTypePreference.preferredFaceTypeTag();
            matchingApplication.preferredDatingStyleTag = idealTypePreference.preferredDatingStyleTag();
        }

        return matchingApplication;
    }

    public IdealTypePreference getIdealTypePreference() {
        return IdealTypePreference.of(
                preferredPersonalityTag,
                preferredFaceTypeTag,
                preferredDatingStyleTag
        );
    }


    public void complete(LocalDateTime completedAt) {
        this.applicationStatus = ApplicationStatus.SUCCESS;
        this.completedAt = requireNonNull(completedAt);
    }

    public void cancel(LocalDateTime cancelledAt) {
        if (applicationStatus.equals(ApplicationStatus.SUCCESS)) {
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