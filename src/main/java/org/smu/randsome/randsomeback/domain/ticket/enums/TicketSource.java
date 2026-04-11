package org.smu.randsome.randsomeback.domain.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketSource {

    JOIN       ("회원 가입으로 인한 티켓 지급"),
    ATTENDANCE ("출석 체크로 인한 티켓 지급"),
    COUPON     ("쿠폰 사용으로 인한 티켓 지급"),
    MATCHING   ("매칭 사용으로 인한 티켓 차감"),
    ADMIN      ("관리자가 수동으로 티켓을 지급하거나 차감하는 경우"),
    ;

    private final String description;
}