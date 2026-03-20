package org.smu.randsome.randsomeback.domain.feed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;

@Tag(name = "Feed Docs", description = "피드 관련 API 문서")
public abstract class FeedControllerDocs {

    @Operation(
            summary = "최신 피드 조회 API - JWT [X]",
            description = """
                    ### 최신 피드 조회 API입니다.
                    - 관리자가 승인한 매칭 신청 이벤트를 최신순으로 조회합니다.
                    - `lastId`를 전달하면 해당 ID 이후의 피드를 커서 기반으로 페이징합니다.
                    - `lastId`를 생략하면 가장 최신 피드부터 조회합니다.
                    - 성공 시 200 OK 와 함께 피드 목록이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<List<FeedItem>> getLatestFeed(
            @Parameter(
                    description = "커서 기반 페이징을 위한 마지막 피드 ID (생략 시 최신순 조회)",
                    in = ParameterIn.QUERY,
                    example = "10"
            )
            Long lastId
    );

}
