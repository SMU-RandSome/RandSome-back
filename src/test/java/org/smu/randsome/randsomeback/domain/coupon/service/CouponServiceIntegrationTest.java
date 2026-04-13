package org.smu.randsome.randsomeback.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class CouponServiceIntegrationTest extends IntegrationTestSupport {

    private final CouponService couponService;
    private final CouponRepository couponRepository;
    private final CouponEventJpaRepository couponEventJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final TicketJpaRepository ticketJpaRepository;

    @Test
    void 쿠폰_사용_시_쿠폰_상태가_USED로_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, member));
        ticketJpaRepository.save(Ticket.create(member, CuponFixture.REWARD_TICKET_TYPE, 0));

        // when
        couponService.useCoupon(coupon.getId(), member.getId());

        // then
        Coupon used = couponRepository.findByIdAndStatusWithEvent(coupon.getId(), org.smu.randsome.randsomeback.global.entity.EntityStatus.ACTIVE).orElseThrow();
        assertThat(used.getCouponStatus()).isEqualTo(CouponStatus.USED);
        assertThat(used.getUsedAt()).isNotNull();
    }

    @Test
    void 쿠폰_사용_시_이벤트에_설정된_티켓_수량만큼_지급된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, member));
        int initialQuantity = 5;
        ticketJpaRepository.save(Ticket.create(member, CuponFixture.REWARD_TICKET_TYPE, initialQuantity));

        // when
        couponService.useCoupon(coupon.getId(), member.getId());

        // then
        Ticket ticket = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(member.getId(), CuponFixture.REWARD_TICKET_TYPE, org.smu.randsome.randsomeback.global.entity.EntityStatus.ACTIVE).orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(initialQuantity + CuponFixture.REWARD_TICKET_QUANTITY);
    }

    @Test
    void 존재하지_않는_쿠폰_사용_시_NOT_FOUND_COUPON_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        Long nonExistentCouponId = 999L;

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(nonExistentCouponId, member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_COUPON.getMessage());
    }

    @Test
    void 타인_쿠폰_사용_시_NOT_FOUND_COUPON_예외가_발생한다() {
        // given
        var owner = memberJpaRepository.save(MemberFixture.create());
        var requester = memberJpaRepository.save(MemberFixture.createWithGender("202312346@sangmyung.kr", Gender.FEMALE));
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, owner));

        // when & then
        // 예외 이후 같은 트랜잭션 내 DB 조회 불가(rollback-only) — 예외 발생 자체로 검증
        assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), requester.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_COUPON.getMessage());
    }

    @Test
    void 이미_사용된_쿠폰_재사용_시_COUPON_NOT_USABLE_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, member));
        ticketJpaRepository.save(Ticket.create(member, CuponFixture.REWARD_TICKET_TYPE, 5));
        coupon.use(); // 이미 사용

        // when & then
        // 예외 이후 같은 트랜잭션 내 DB 조회 불가(rollback-only) — 예외 발생 자체로 검증
        assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }

}
