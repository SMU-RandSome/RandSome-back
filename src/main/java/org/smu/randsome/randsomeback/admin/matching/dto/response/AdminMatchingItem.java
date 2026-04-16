package org.smu.randsome.randsomeback.admin.matching.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;

@Schema(description = "관리자 매칭 신청 항목")
@Builder
public record AdminMatchingItem(
        @Schema(description = "매칭 신청 ID")
        Long id,

        @Schema(description = "신청자 닉네임")
        String applicantNickname,

        @Schema(description = "신청자 법정 이름")
        String applicantLegalName,

        @Schema(description = "신청자 성별")
        Gender applicantGender,

        @Schema(description = "매칭 타입")
        MatchingType matchingType,

        @Schema(description = "신청 인원 수")
        int applicationCount,

        @Schema(description = "신청 상태")
        ApplicationStatus applicationStatus,

        @Schema(description = "신청 일시")
        LocalDateTime createdAt
) {

    public static AdminMatchingItem from(MatchingApplication application) {
        Member applicant = application.getMember();

        return AdminMatchingItem.builder()
                .id(application.getId())
                .applicantNickname(applicant.getNickname())
                .applicantLegalName(applicant.getLegalName())
                .applicantGender(applicant.getGender())
                .matchingType(application.getMatchingType())
                .applicationCount(application.getApplicationCount())
                .applicationStatus(application.getApplicationStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }

}