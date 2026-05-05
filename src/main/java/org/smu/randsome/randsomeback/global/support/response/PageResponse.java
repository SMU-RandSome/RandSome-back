package org.smu.randsome.randsomeback.global.support.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;

@Schema(description = "페이지 응답")
public record PageResponse<T>(

        @Schema(description = "조회 결과 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호", example = "1")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 데이터 수", example = "15")
        long totalElements,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                (long) page * size < totalElements
        );
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(
                content.stream()
                        .map(mapper)
                        .toList(),
                page,
                size,
                totalElements,
                hasNext
        );
    }

}
