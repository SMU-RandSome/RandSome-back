package org.smu.randsome.randsomeback.domain.announcement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;

@Schema(description = "공지사항 항목")
@Builder
public record AnnouncementItem(
        @Schema(description = "공지사항 ID", example = "1")
        Long id,

        @Schema(description = "공지사항 제목", example = "새로운 공지사항 제목")
        String title,

        @Schema(description = "공지사항 내용", example = "공지사항 내용을 입력하세요.")
        String content,

        @Schema(description = "공지사항 생성 시각")
        LocalDateTime createdAt
) {

    public static AnnouncementItem from(Announcement announcement) {
        return AnnouncementItem.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .createdAt(announcement.getCreatedAt())
                .build();
    }

}