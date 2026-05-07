package org.smu.randsome.randsomeback.admin.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.admin.ticket.dto.request.TicketDeductRequest;
import org.smu.randsome.randsomeback.admin.ticket.dto.request.TicketEarnRequest;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 티켓 관리 API", description = "관리자용 티켓 수동 지급/차감 API")
public abstract class TicketAdminControllerDocs {

    @Operation(summary = "티켓 수동 지급", description = """
            관리자가 특정 회원에게 수동으로 티켓을 지급하는 API입니다.
            - 대상 회원 ID, 티켓 종류(RANDOM/IDEAL), 지급 수량을 지정합니다.
            - 선택적으로 지급 사유를 기록할 수 있습니다.
            """)
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.NOT_FOUND_TICKET,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> earnTicket(@RequestBody @Valid TicketEarnRequest request);

    @Operation(summary = "티켓 수동 차감", description = """
            관리자가 특정 회원의 티켓을 수동으로 차감하는 API입니다.
            - 대상 회원 ID, 티켓 종류(RANDOM/IDEAL), 차감 수량을 지정합니다.
            - 선택적으로 차감 사유를 기록할 수 있습니다.
            - 잔액이 부족할 경우 예외가 발생합니다.
            """)
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.NOT_FOUND_TICKET,
            ErrorType.NOT_ENOUGH_TICKETS,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> deductTicket(@RequestBody @Valid TicketDeductRequest request);
}
