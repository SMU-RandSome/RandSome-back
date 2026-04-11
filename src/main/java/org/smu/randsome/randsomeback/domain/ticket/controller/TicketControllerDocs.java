package org.smu.randsome.randsomeback.domain.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
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

}