package org.smu.randsome.randsomeback.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearch;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class PaymentQueryRepositoryIntegrationTest extends IntegrationTestSupport {

    final PaymentRepository paymentRepository;
    final MemberJpaRepository memberJpaRepository;
    final CandidateJpaRepository candidateJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;

    // ===== 상태 필터링 =====

    @Test
    void PENDING_상태로_조회하면_대기_중인_결제만_반환된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var reg = candidateJpaRepository.save(CandidateRegistration.apply(member));

        var pending = paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg.getId(), 1));
        var completed = paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg.getId() + 1, 1));
        completed.confirm(TestDateTimeUtils.now());

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), ""),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(pr -> pr.payment().getId(), PaymentWithReason::rejectedReason)
                .containsExactly(tuple(pending.getId(), null));
    }

    @Test
    void COMPLETED와_REJECTED_상태로_조회하면_처리된_결제만_반환된다() {
        // given: 후보자 결제 1건 + 매칭 결제 1건을 각각 완료/거절 처리
        var member = memberJpaRepository.save(MemberFixture.create());
        var reg1 = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var reg2 = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));

        // PENDING → 조회 제외
        paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg1.getId(), 1));

        var completed = paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg2.getId(), 1));
        completed.confirm(TestDateTimeUtils.now());

        var rejected = paymentRepository.save(Payment.register(member, PaymentType.RANDOM_MATCHING, application.getId(), 1));
        rejected.reject(TestDateTimeUtils.now());

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.COMPLETED, PaymentStatus.REJECTED), ""),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(pr -> pr.payment().getPaymentStatus())
                .containsExactlyInAnyOrder(PaymentStatus.COMPLETED, PaymentStatus.REJECTED);
    }

    @Test
    void 해당_상태의_결제가_없으면_빈_페이지를_반환한다() {
        // given: PENDING 결제만 존재
        var member = memberJpaRepository.save(MemberFixture.create());
        var reg = candidateJpaRepository.save(CandidateRegistration.apply(member));
        paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg.getId(), 1));

        var pageable = PageRequest.of(0, 10);

        // when: COMPLETED 상태로 조회
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.COMPLETED), ""),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    // ===== 후보자 등록 결제 - rejectedReason =====

    @Test
    void 거절된_후보자_등록_결제_조회_시_거절_사유가_포함된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var reason = "증빙 서류 미비";
        registration.reject(reason, TestDateTimeUtils.now());

        var payment = paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, registration.getId(), 1));
        payment.reject(TestDateTimeUtils.now());

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.REJECTED), ""),
                pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().rejectedReason()).isEqualTo(reason);
    }

    @Test
    void 거절_사유가_없는_결제는_rejectedReason이_null이다() {
        // given: PENDING이므로 거절 사유 없음
        var member = memberJpaRepository.save(MemberFixture.create());
        var reg = candidateJpaRepository.save(CandidateRegistration.apply(member));
        paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg.getId(), 1));

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), ""),
                pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().rejectedReason()).isNull();
    }

    // ===== 매칭 신청 결제 - rejectedReason =====

    @Test
    void 거절된_랜덤_매칭_결제_조회_시_매칭_신청의_거절_사유가_포함된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 2));
        var reason = "인원 부족";
        application.reject(TestDateTimeUtils.now(), reason);

        var payment = paymentRepository.save(Payment.register(member, PaymentType.RANDOM_MATCHING, application.getId(), 2));
        payment.reject(TestDateTimeUtils.now());

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.REJECTED), ""),
                pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().rejectedReason()).isEqualTo(reason);
    }

    @Test
    void 거절된_이상형_매칭_결제_조회_시_매칭_신청의_거절_사유가_포함된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.IDEAL, 1));
        var reason = "조건 불일치";
        application.reject(TestDateTimeUtils.now(), reason);

        var payment = paymentRepository.save(Payment.register(member, PaymentType.IDEAL_TYPE_MATCHING, application.getId(), 1));
        payment.reject(TestDateTimeUtils.now());

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.REJECTED), ""),
                pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().rejectedReason()).isEqualTo(reason);
    }

    // ===== 결제 유형 혼재 =====

    @Test
    void 후보자_등록과_매칭_신청_결제가_함께_조회되며_각_거절_사유를_올바르게_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        // 후보자 등록 결제 거절
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var candidateReason = "자격 미달";
        registration.reject(candidateReason, TestDateTimeUtils.now());
        var candidatePayment = paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, registration.getId(), 1));
        candidatePayment.reject(TestDateTimeUtils.now());

        // 랜덤 매칭 결제 거절
        var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        var matchingReason = "인원 부족";
        application.reject(TestDateTimeUtils.now(), matchingReason);
        var matchingPayment = paymentRepository.save(Payment.register(member, PaymentType.RANDOM_MATCHING, application.getId(), 1));
        matchingPayment.reject(TestDateTimeUtils.now());

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.REJECTED), ""),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(pr -> pr.payment().getPaymentType(), PaymentWithReason::rejectedReason)
                .containsExactlyInAnyOrder(
                        tuple(PaymentType.CANDIDATE_REGISTRATION, candidateReason),
                        tuple(PaymentType.RANDOM_MATCHING, matchingReason)
                );
    }

    // ===== 소프트 삭제 필터링 =====

    @Test
    void 소프트_삭제된_결제는_조회되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var reg1 = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var reg2 = candidateJpaRepository.save(CandidateRegistration.apply(member));

        var deleted = paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg1.getId(), 1));
        deleted.delete();

        paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg2.getId(), 1));

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), ""),
                pageable
        );

        // then: 삭제된 결제 제외, ACTIVE 결제 1건만 조회
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(pr -> pr.payment().getId())
                .doesNotContain(deleted.getId());
    }

    // ===== 페이지네이션 =====

    @Test
    void 페이지네이션이_올바르게_동작한다() {
        // given: 후보자 결제 3건 + 매칭 결제 2건
        var member = memberJpaRepository.save(MemberFixture.create());

        for (int i = 0; i < 3; i++) {
            var reg = candidateJpaRepository.save(CandidateRegistration.apply(member));
            paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg.getId(), 1));
        }
        for (int i = 0; i < 2; i++) {
            var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, i + 1));
            paymentRepository.save(Payment.register(member, PaymentType.RANDOM_MATCHING, application.getId(), i + 1));
        }

        var firstPage = PageRequest.of(0, 2);
        var lastPage  = PageRequest.of(2, 2);

        // when
        var page1 = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), ""), firstPage);
        var page3 = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), ""), lastPage);

        // then
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page3.getContent()).hasSize(1);
    }

    // ===== 키워드 검색 =====

    @Test
    void 이름_키워드로_검색하면_해당_회원의_결제만_반환된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.create()); // "홍길동"
        var member2 = memberJpaRepository.save(MemberFixture.createWithLegalName("202300001@sangmyung.kr", "김철수"));

        var reg1 = candidateJpaRepository.save(CandidateRegistration.apply(member1));
        var reg2 = candidateJpaRepository.save(CandidateRegistration.apply(member2));
        paymentRepository.save(Payment.register(member1, PaymentType.CANDIDATE_REGISTRATION, reg1.getId(), 1));
        paymentRepository.save(Payment.register(member2, PaymentType.CANDIDATE_REGISTRATION, reg2.getId(), 1));

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), "홍"),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().payment().getMember().getLegalName()).isEqualTo("홍길동");
    }

    @Test
    void 검색어가_없으면_모든_결제가_반환된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.create()); // "홍길동"
        var member2 = memberJpaRepository.save(MemberFixture.createWithLegalName("202300001@sangmyung.kr", "김철수"));

        var reg1 = candidateJpaRepository.save(CandidateRegistration.apply(member1));
        var reg2 = candidateJpaRepository.save(CandidateRegistration.apply(member2));
        paymentRepository.save(Payment.register(member1, PaymentType.CANDIDATE_REGISTRATION, reg1.getId(), 1));
        paymentRepository.save(Payment.register(member2, PaymentType.CANDIDATE_REGISTRATION, reg2.getId(), 1));

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), ""),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 대소문자_구분없이_검색된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.createWithLegalName("202300002@sangmyung.kr", "HongGilDong"));
        var reg = candidateJpaRepository.save(CandidateRegistration.apply(member));
        paymentRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, reg.getId(), 1));

        var pageable = PageRequest.of(0, 10);

        // when
        var result = paymentRepository.findPaymentsWithRejectedReason(
                new PaymentSearch(List.of(PaymentStatus.PENDING), "honggildong"),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().payment().getMember().getLegalName()).isEqualTo("HongGilDong");
    }

}