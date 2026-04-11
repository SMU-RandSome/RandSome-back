package org.smu.randsome.randsomeback.global.support.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 페이지 응답")
public record CursorSlice<T>(

        @Schema(description = "조회 결과 목록")
        List<T> items,

        @Schema(description = "다음 페이지 커서 (null이면 마지막 페이지)")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {

    public static <T> CursorSlice<T> of(List<T> items, Long nextCursor, boolean hasNext) {
        return new CursorSlice<>(items, nextCursor, hasNext);
    }

}