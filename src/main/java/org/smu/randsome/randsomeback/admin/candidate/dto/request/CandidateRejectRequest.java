package org.smu.randsome.randsomeback.admin.candidate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "후보자 등록 거절 요청", description = "관리자가 후보자 등록을 거절할 때 요청하는 정보입니다.")
public record CandidateRejectRequest(
        @Schema(description = "거절 사유", example = "제출된 서류가 불충분합니다.")
        @NotBlank(message = "거절 사유는 필수입니다.")
        String reason
) {

}