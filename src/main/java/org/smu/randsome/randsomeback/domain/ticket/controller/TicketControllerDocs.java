package org.smu.randsome.randsomeback.domain.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.domain.ticket.dto.response.TicketHistoryItem;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketHistorySortType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;

@Tag(name = "Ticket Docs", description = "회원의 관련 API 문서")
public abstract class TicketControllerDocs {

    @Operation(summary = "티켓 잔고 조회 - JWT [O]",
            description = """
                    회원의 티켓 잔고를 조회한다.
                    - 랜덤 매칭 티켓과 이상형 매칭 티켓의 수량을 반환한다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_TICKET,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> getTicketBalance(@LoginMember Long memberId);

    @Operation(summary = "티켓 변동 이력 조회 - JWT [O]",
            description = """
                    회원의 티켓 변동 이력을 커서 기반으로 조회한다.
                    - ticketType 미전달 시 전체 유형 조회
                    - sortType: LATEST(최신순, 기본값) / OLDEST(오래된순)
                    - cursor 미전달 시 첫 페이지 조회
                    - size 기본값: 20
                    """
    )
    @ApiExceptions(values = {ErrorType.DEFAULT_ERROR})
    public abstract ApiResponse<CursorSlice<TicketHistoryItem>> findMyTicketsHistory(
            @Parameter(description = "티켓 유형 필터 (미전달 시 전체)", example = "RANDOM")
            TicketType ticketType,
            @Parameter(description = "정렬 기준", example = "LATEST")
            TicketHistorySortType sortType,
            @Parameter(description = "이전 페이지의 마지막 ID (첫 페이지는 미전달)", example = "42")
            Long cursor,
            @Parameter(description = "페이지 크기", example = "20")
            int size,
            Long memberId
    );

}