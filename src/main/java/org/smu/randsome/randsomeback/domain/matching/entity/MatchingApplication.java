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

/**
 * 매칭 신청을 나타내는 엔티티다.
 * <br/>회원이 매칭을 신청하면 이 엔티티를 통해 신청 정보가 관리되며, 상태는 라이프사이클을 따른다.
 * <br/>상태 전이: PENDING → SUCCESS (또는) PENDING → CANCELLED
 * <br/>이상형 매칭인 경우 선호하는 성격, 얼굴상, 연애 스타일을 저장하여 매칭 필터링에 활용한다.
 */
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

    /**
     * 이상형 조건 없이 매칭 신청을 생성한다 (주로 랜덤 매칭 용).
     *
     * @param member 신청자
     * @param matchingType 매칭 타입 (`RANDOM` 또는 `IDEAL`)
     * @param applicationCount 신청 인원 수 (1~5)
     * @return 초기화된 매칭 신청 엔티티 (상태: PENDING)
     * @throws CoreException 신청 인원 수가 범위 밖인 경우
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
     *
     * @param member 신청자
     * @param matchingType 매칭 타입 (`RANDOM` 또는 `IDEAL`)
     * @param applicationCount 신청 인원 수 (1~5)
     * @param idealTypePreference 이상형 조건 (nullable, 랜덤 매칭 시 null)
     * @return 초기화된 매칭 신청 엔티티 (상태: PENDING)
     * @throws CoreException 신청 인원 수가 범위 밖인 경우
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

        if (idealTypePreference != null) {
            matchingApplication.preferredPersonalityTag = idealTypePreference.preferredPersonalityTag();
            matchingApplication.preferredFaceTypeTag = idealTypePreference.preferredFaceTypeTag();
            matchingApplication.preferredDatingStyleTag = idealTypePreference.preferredDatingStyleTag();
        }

        return matchingApplication;
    }

    /**
     * 이 신청에 저장된 이상형 조건을 Value Object로 반환한다.
     * <br/>랜덤 매칭의 경우 모든 필드가 null이 될 수 있다.
     *
     * @return 이상형 조건 Value Object
     */
    public IdealTypePreference getIdealTypePreference() {
        return IdealTypePreference.of(
                preferredPersonalityTag,
                preferredFaceTypeTag,
                preferredDatingStyleTag
        );
    }

    /**
     * 매칭 신청을 완료 상태로 전이한다.
     * <br/>매칭 알고리즘이 완료되어 결과가 생성되었을 때 호출된다.
     *
     * @param completedAt 매칭 완료 시각
     */
    public void complete(LocalDateTime completedAt) {
        this.applicationStatus = ApplicationStatus.SUCCESS;
        this.completedAt = requireNonNull(completedAt);
    }

    /**
     * 매칭 신청을 취소한다.
     * <br/>PENDING 상태에서만 취소 가능하며, 이미 매칭된 신청(SUCCESS)은 취소할 수 없다.
     * <br/>이미 취소된 신청의 경우 idempotent하게 처리한다 (아무 변화 없음).
     *
     * @param cancelledAt 취소 시각
     * @throws CoreException 매칭이 이미 완료된 경우 (SUCCESS 상태)
     */
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

    /**
     * 신청자의 성별을 기준으로 매칭 대상 성별을 반환한다.
     * <br/>남성 신청자 → 여성 대상, 여성 신청자 → 남성 대상
     *
     * @return 매칭 대상 성별
     */
    public Gender getTargetGender() {
        return this.getMember().getGender() == Gender.MALE ? Gender.FEMALE : Gender.MALE;
    }

    private static void validateApplicationCount(int applicationCount) {
        if (applicationCount < 1 || applicationCount > 5) {
            throw new CoreException(ErrorType.INVALID_PERSON_COUNT);
        }
    }

}