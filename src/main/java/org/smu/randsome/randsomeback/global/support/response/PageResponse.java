package org.smu.randsome.randsomeback.global.support.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "페이지 응답")
public record PageResponse<T>(

        @Schema(description = "조회 결과 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "2")
        int totalPages,

        @Schema(description = "전체 데이터 수", example = "15")
        long totalElements,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

}