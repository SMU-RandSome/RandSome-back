package org.smu.randsome.randsomeback.admin.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;

@Schema(description = "쿠폰 발급 회원 정보")
public record CouponIssuedMemberItem(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "닉네임", example = "남자_abcd1234")
        String nickname,

        @Schema(description = "실명", example = "홍길동")
        String legalName,

        @Schema(description = "쿠폰 상태", example = "AVAILABLE")
        CouponStatus couponStatus,

        @Schema(description = "쿠폰 발급 일시", example = "2025-06-01T10:30:00")
        LocalDateTime issuedAt
) {

    public static CouponIssuedMemberItem from(Coupon coupon) {
        return new CouponIssuedMemberItem(
                coupon.getMember().getId(),
                coupon.getMember().getNickname(),
                coupon.getMember().getLegalName(),
                coupon.getCouponStatus(),
                coupon.getCreatedAt()
        );
    }

}