package org.smu.randsome.randsomeback.domain.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketSource {

    JOIN                 ("회원 가입으로 인한 티켓 지급"),
    ATTENDANCE           ("출석 체크로 인한 티켓 지급"),
    COUPON               ("쿠폰 사용으로 인한 티켓 지급"),
    MATCHING             ("매칭 사용으로 인한 티켓 차감"),
    PARTIAL_MATCH_REFUND ("신청 인원 보다 적은 매칭 인원으로 인한 티켓 환불"),
    NO_MATCH_REFUND      ("매칭 실패로 인한 티켓 환불"),
    ADMIN                ("관리자가 수동으로 티켓을 지급하거나 차감하는 경우"),
    ;

    private final String description;
}