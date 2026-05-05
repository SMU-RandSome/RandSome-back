package org.smu.randsome.randsomeback.admin.announcement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.smu.randsome.randsomeback.domain.announcement.dto.command.NewAnnouncement;

@Schema(description = "공지사항 등록 요청")
public record AnnouncementRegisterRequest(
        @Schema(description = "공지사항 제목", example = "새로운 공지사항 제목")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "공지사항 내용", example = "공지사항 내용을 입력하세요.")
        @NotBlank(message = "내용은 필수입니다.")
        String content
) {

    public NewAnnouncement toNewAnnouncement() {
        return NewAnnouncement.builder()
                .title(title)
                .content(content)
                .build();
    }

}