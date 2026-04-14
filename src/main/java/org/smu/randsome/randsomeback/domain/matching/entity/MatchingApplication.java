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
import java.math.BigDecimal;
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

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    private String rejectedReason;

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

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

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
        matchingApplication.totalPrice = matchingType.calculateFee(applicationCount);
        matchingApplication.rejectedReason = null;
        matchingApplication.applicationStatus = ApplicationStatus.PENDING;
        matchingApplication.approvedAt = null;
        matchingApplication.rejectedAt = null;
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

    public void approve(LocalDateTime approvedAt) {
        if (applicationStatus.equals(ApplicationStatus.APPROVED)) {
            return;
        }
        checkCancel();
        // NOTE: REJECTED → APPROVED 재승인 허용.
        // 관리자 실수 정정을 위해 의도적으로 허용. 이 시점에 매칭 결과는 미생성이므로 중복 없음.
        this.applicationStatus = ApplicationStatus.APPROVED;
        this.approvedAt = requireNonNull(approvedAt);
        this.rejectedReason = null;
        this.rejectedAt = null;
    }

    public void reject(LocalDateTime rejectedAt, String rejectedReason) {
        if (applicationStatus.equals(ApplicationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING);
        }
        checkCancel();

        this.applicationStatus = ApplicationStatus.REJECTED;
        this.rejectedAt = requireNonNull(rejectedAt);
        this.rejectedReason = requireNonNull(rejectedReason);
        this.approvedAt = null;
    }

    public void cancel(LocalDateTime cancelledAt) {
        if (applicationStatus.equals(ApplicationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_CANCEL_APPROVED);
        }
        if (applicationStatus.equals(ApplicationStatus.REJECTED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_CANCEL_REJECTED);
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

    private void checkCancel() {
        if (applicationStatus.equals(ApplicationStatus.CANCELLED)) {
            throw new CoreException(ErrorType.ALREADY_CANCELLED_MATCHING);
        }
    }

}
