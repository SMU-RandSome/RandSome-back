package org.smu.randsome.randsomeback.admin.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Schema(description = "쿠폰 이벤트 생성 요청")
public record CouponEventRegisterRequest(

        @Schema(description = "이벤트 이름", example = "선착순 랜덤 매칭 티켓 증정")
        @NotBlank(message = "이벤트 이름은 필수입니다.")
        String name,

        @Schema(description = "이벤트 설명", example = "선착순 100명에게 랜덤 티켓을 드립니다.")
        String description,

        @Schema(description = "이벤트 타입", example = "HAPPY_HOUR")
        @NotNull(message = "이벤트 타입은 필수입니다.")
        CouponEventType type,

        @Schema(description = "총 쿠폰 수량", example = "100")
        @Min(value = 1, message = "총 수량은 1 이상이어야 합니다.")
        int totalQuantity,

        @Schema(description = "지급할 티켓 타입", example = "RANDOM")
        @NotNull(message = "리워드 티켓 타입은 필수입니다.")
        TicketType rewardTicketType,

        @Schema(description = "쿠폰 1개당 지급되는 티켓 수량", example = "1")
        @Min(value = 1, message = "리워드 티켓 수량은 1 이상이어야 합니다.")
        int rewardTicketAmount,

        @Schema(description = "이벤트 시작 시각", example = "2026-05-01T10:00:00")
        @NotNull(message = "시작 시각은 필수입니다.")
        LocalDateTime startsAt,

        @Schema(description = "이벤트 종료 시각", example = "2026-05-01T18:00:00")
        @NotNull(message = "종료 시각은 필수입니다.")
        LocalDateTime expiresAt
) {

    public NewCouponEvent toNewCouponEvent() {
        return NewCouponEvent.builder()
                .name(name)
                .description(description)
                .type(type)
                .totalQuantity(totalQuantity)
                .rewardTicketType(rewardTicketType)
                .rewardTicketAmount(rewardTicketAmount)
                .startsAt(startsAt)
                .expiresAt(expiresAt)
                .build();
    }

}