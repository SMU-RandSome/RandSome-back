package org.smu.randsome.randsomeback.domain.matching.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

/**
 * 매칭 신청을 나타내는 엔티티다.
 * <br/>회원이 매칭을 신청하면 이 엔티티를 통해 신청 정보가 관리되며, 상태는 라이프사이클을 따른다.
 * <br/>상태 전이: PENDING → SUCCESS / PARTIAL_MATCH / FAILED (또는) PENDING → CANCELLED
 * <br/>이상형 매칭의 선호 태그는 {@link MatchingIdealTypeSnapshot}에 별도 스냅샷으로 저장된다.
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

    @Version
    private Long version;

    private Integer matchedCount;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    public static MatchingApplication apply(
            Member member,
            MatchingType matchingType,
            Integer applicationCount
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

        return matchingApplication;
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
