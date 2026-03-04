package org.smu.randsome.randsomeback.global.support.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Schema(description = "API 공통 응답")
public record ApiResponse<T>(
        @Schema(description = "응답 결과 유형", example = "SUCCESS")
        ResultType result,

        @Schema(description = "응답 데이터")
        T data,

        @Schema(description = "에러 정보")
        ErrorMessage error
) {

    public static ApiResponse<?> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<?> error(ErrorType error) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error));
    }

    public static ApiResponse<?> error(ErrorType error, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error, errorData));
    }

}