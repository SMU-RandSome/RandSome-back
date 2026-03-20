package org.smu.randsome.randsomeback.global.config;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.feed.MatchingFeedEvent;
import org.smu.randsome.randsomeback.domain.feed.MatchingFeedEventRepository;
import org.smu.randsome.randsomeback.domain.bankaccount.entity.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.repository.BankAccountJpaRepository;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Profile("local")
@Component
@RequiredArgsConstructor
public class LocalDataInitializer implements ApplicationRunner {

    private static final String RAW_PASSWORD = "Test1234!";

    private final MemberJpaRepository memberJpaRepository;
    private final CandidateJpaRepository candidateJpaRepository;
    private final BankAccountJpaRepository bankAccountJpaRepository;
    private final MatchingJpaRepository matchingJpaRepository;
    private final MatchingResultJpaRepository matchingResultJpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;
    private final AnnouncementJpaRepository announcementJpaRepository;
    private final MatchingFeedEventRepository matchingFeedEventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (alreadyInitialized()) {
            log.info("[LocalDataInitializer] 시드 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("[LocalDataInitializer] 로컬 시드 데이터를 생성합니다. (비밀번호: {})", RAW_PASSWORD);

        final LocalDateTime now = LocalDateTime.now();

        // ─── 관리자 ────────────────────────────────────────────────────────────────
        Member admin = createMember("202100001@sangmyung.kr", "관리자", Gender.MALE, Mbti.ENTJ, "admin_insta");
        admin.updateRole(Role.ROLE_ADMIN);
        memberJpaRepository.save(admin);
        saveBankAccount(admin, "국민", "111111111111");

        // ─── 공지사항 ───────────────────────────────────────────────────────────────
        announcementJpaRepository.save(Announcement.register(admin,
            "2024 상명대 랜섬 서비스 오픈 안내",
            "안녕하세요! 랜섬 서비스가 정식 오픈되었습니다. 많은 이용 부탁드립니다."));
        announcementJpaRepository.save(Announcement.register(admin,
            "3월 매칭 신청 기간 안내",
            "3월 매칭 신청 기간은 3/1~3/15입니다. 기간 내 신청 부탁드립니다."));
        announcementJpaRepository.save(Announcement.register(admin,
            "후보자 모집 안내",
            "후보자 지원은 상시 가능합니다. 지원 후 관리자 승인을 받아야 매칭에 참여할 수 있습니다."));

        // ─── 승인된 후보자 (APPROVED) ─────────────────────────────────────────────
        List<Member> approvedMaleCandidates = List.of(
            createMember("202200001@sangmyung.kr", "김철수", Gender.MALE, Mbti.ISTP, "male_cand_1"),
            createMember("202200002@sangmyung.kr", "박민준", Gender.MALE, Mbti.ENFP, "male_cand_2"),
            createMember("202300001@sangmyung.kr", "이준호", Gender.MALE, Mbti.INTJ, "male_cand_3")
        );
        List<Member> approvedFemaleCandidates = List.of(
            createMember("202200101@sangmyung.kr", "김지원", Gender.FEMALE, Mbti.INFJ, "female_cand_1"),
            createMember("202200102@sangmyung.kr", "박서연", Gender.FEMALE, Mbti.ESFJ, "female_cand_2"),
            createMember("202300101@sangmyung.kr", "이수아", Gender.FEMALE, Mbti.ENFJ, "female_cand_3")
        );
        saveApprovedCandidates(approvedMaleCandidates, now);
        saveApprovedCandidates(approvedFemaleCandidates, now);

        // ─── 대기 중인 후보자 신청 (PENDING) ──────────────────────────────────────
        Member pendingMaleCandidate = createMember("202201001@sangmyung.kr", "최민재", Gender.MALE, Mbti.ENFP, "pending_male_cand");
        Member pendingFemaleCandidate = createMember("202201101@sangmyung.kr", "한채원", Gender.FEMALE, Mbti.ISFJ, "pending_female_cand");
        memberJpaRepository.save(pendingMaleCandidate);
        memberJpaRepository.save(pendingFemaleCandidate);
        savePendingCandidateRegistration(pendingMaleCandidate);
        savePendingCandidateRegistration(pendingFemaleCandidate);

        // ─── 거절된 후보자 신청 (REJECTED) ────────────────────────────────────────
        Member rejectedMaleCandidate = createMember("202201002@sangmyung.kr", "오동현", Gender.MALE, Mbti.ESTP, "rejected_male_cand");
        Member rejectedFemaleCandidate = createMember("202201102@sangmyung.kr", "윤지은", Gender.FEMALE, Mbti.INTP, "rejected_female_cand");
        memberJpaRepository.save(rejectedMaleCandidate);
        memberJpaRepository.save(rejectedFemaleCandidate);
        saveRejectedCandidateRegistration(rejectedMaleCandidate, "후보자 요건 미충족", now);
        saveRejectedCandidateRegistration(rejectedFemaleCandidate, "제출 서류 불충분", now);

        // ─── 철회된 후보자 신청 (WITHDRAWN) ───────────────────────────────────────
        Member withdrawnMaleCandidate = createMember("202201003@sangmyung.kr", "강동훈", Gender.MALE, Mbti.INFP, "withdrawn_male_cand");
        Member withdrawnFemaleCandidate = createMember("202201103@sangmyung.kr", "임소영", Gender.FEMALE, Mbti.ENTJ, "withdrawn_female_cand");
        memberJpaRepository.save(withdrawnMaleCandidate);
        memberJpaRepository.save(withdrawnFemaleCandidate);
        saveWithdrawnCandidateRegistration(withdrawnMaleCandidate, now);
        saveWithdrawnCandidateRegistration(withdrawnFemaleCandidate, now);

        // ─── 일반 회원 ──────────────────────────────────────────────────────────────
        Member regularMale1 = createMember("202400001@sangmyung.kr", "정태양", Gender.MALE, Mbti.ESTP, "regular_male_1");
        Member regularMale2 = createMember("202400002@sangmyung.kr", "배준혁", Gender.MALE, Mbti.ISTJ, "regular_male_2");
        Member regularMale3 = createMember("202400003@sangmyung.kr", "신민호", Gender.MALE, Mbti.ENTP, "regular_male_3");
        Member regularFemale1 = createMember("202400101@sangmyung.kr", "홍미래", Gender.FEMALE, Mbti.ISFP, "regular_female_1");
        Member regularFemale2 = createMember("202400102@sangmyung.kr", "이지혜", Gender.FEMALE, Mbti.ESFP, "regular_female_2");
        Member regularFemale3 = createMember("202400103@sangmyung.kr", "김수빈", Gender.FEMALE, Mbti.INFP, "regular_female_3");

        List<Member> regularMembers = List.of(
            regularMale1, regularMale2, regularMale3,
            regularFemale1, regularFemale2, regularFemale3
        );
        memberJpaRepository.saveAll(regularMembers);
        regularMembers.forEach(m -> saveBankAccount(m, "신한", "222222222222"));

        // ─── 매칭 신청 ─────────────────────────────────────────────────────────────

        // 1. APPROVED + 결과 있음 (정태양 남성 → RANDOM 3명 → 여성 후보 3명 매칭)
        MatchingApplication approvedRandom = MatchingApplication.apply(regularMale1, MatchingType.RANDOM, 3);
        approvedRandom.approve(now.minusDays(5));
        matchingJpaRepository.save(approvedRandom);
        Payment approvedRandomPayment = Payment.register(regularMale1, PaymentType.RANDOM_MATCHING, approvedRandom.getId(), 3);
        approvedRandomPayment.confirm(now.minusDays(5));
        paymentJpaRepository.save(approvedRandomPayment);
        saveMatchingResults(approvedRandom, approvedFemaleCandidates);
        matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest(regularMale1.getNickname(), 3));

        // 1-1. REJECTED (정태양 남성 → IDEAL 1명)
        MatchingApplication rejectedRandom1 = MatchingApplication.apply(regularMale1, MatchingType.IDEAL, 1);
        rejectedRandom1.reject(now.minusDays(10), "신청 기간 초과로 인한 거절");
        matchingJpaRepository.save(rejectedRandom1);
        Payment rejectedRandom1Payment = Payment.register(regularMale1, PaymentType.IDEAL_TYPE_MATCHING, rejectedRandom1.getId(), 1);
        rejectedRandom1Payment.reject(now.minusDays(10));
        paymentJpaRepository.save(rejectedRandom1Payment);

        // 1-2. PENDING (정태양 남성 → RANDOM 2명)
        MatchingApplication pendingRandom1 = MatchingApplication.apply(regularMale1, MatchingType.RANDOM, 2);
        matchingJpaRepository.save(pendingRandom1);
        paymentJpaRepository.save(Payment.register(regularMale1, PaymentType.RANDOM_MATCHING, pendingRandom1.getId(), 2));

        // 2. APPROVED + 결과 있음 (홍미래 여성 → IDEAL 2명 → 남성 후보 2명 매칭)
        MatchingApplication approvedIdeal = MatchingApplication.apply(regularFemale1, MatchingType.IDEAL, 2);
        approvedIdeal.approve(now.minusDays(3));
        matchingJpaRepository.save(approvedIdeal);
        Payment approvedIdealPayment = Payment.register(regularFemale1, PaymentType.IDEAL_TYPE_MATCHING, approvedIdeal.getId(), 2);
        approvedIdealPayment.confirm(now.minusDays(3));
        paymentJpaRepository.save(approvedIdealPayment);
        saveMatchingResults(approvedIdeal, approvedMaleCandidates.subList(0, 2));
        matchingFeedEventRepository.save(MatchingFeedEvent.recordMatchRequest(regularFemale1.getNickname(), 2));

        // 3. PENDING (배준혁 남성 → RANDOM 2명)
        MatchingApplication pendingRandom = MatchingApplication.apply(regularMale2, MatchingType.RANDOM, 2);
        matchingJpaRepository.save(pendingRandom);
        paymentJpaRepository.save(Payment.register(regularMale2, PaymentType.RANDOM_MATCHING, pendingRandom.getId(), 2));

        // 4. REJECTED (이지혜 여성 → IDEAL 1명)
        MatchingApplication rejectedIdeal = MatchingApplication.apply(regularFemale2, MatchingType.IDEAL, 1);
        rejectedIdeal.reject(now.minusDays(1), "중복 신청으로 인한 거절");
        matchingJpaRepository.save(rejectedIdeal);
        Payment rejectedIdealPayment = Payment.register(regularFemale2, PaymentType.IDEAL_TYPE_MATCHING, rejectedIdeal.getId(), 1);
        rejectedIdealPayment.reject(now.minusDays(1));
        paymentJpaRepository.save(rejectedIdealPayment);

        // 5. WITHDRAWN (신민호 남성 → RANDOM 1명, 본인이 신청 취소)
        MatchingApplication withdrawnRandom = MatchingApplication.apply(regularMale3, MatchingType.RANDOM, 1);
        withdrawnRandom.withdraw(now.minusDays(2));
        matchingJpaRepository.save(withdrawnRandom);
        paymentJpaRepository.save(Payment.register(regularMale3, PaymentType.RANDOM_MATCHING, withdrawnRandom.getId(), 1));

        // 6. PENDING (김수빈 여성 → IDEAL 3명)
        MatchingApplication pendingIdeal = MatchingApplication.apply(regularFemale3, MatchingType.IDEAL, 3);
        matchingJpaRepository.save(pendingIdeal);
        paymentJpaRepository.save(Payment.register(regularFemale3, PaymentType.IDEAL_TYPE_MATCHING, pendingIdeal.getId(), 3));

        logSummary();
    }

    private void saveApprovedCandidates(List<Member> members, LocalDateTime now) {
        for (Member member : members) {
            member.updateRole(Role.ROLE_CANDIDATE);
            memberJpaRepository.save(member);
            saveBankAccount(member, "카카오", "333333333333");

            CandidateRegistration registration = CandidateRegistration.apply(member);
            registration.approve(now.minusDays(10));
            candidateJpaRepository.save(registration);

            Payment payment = Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, registration.getId(), 1);
            payment.confirm(now.minusDays(10));
            paymentJpaRepository.save(payment);

            matchingFeedEventRepository.save(MatchingFeedEvent.recordCandidateRegister(member.getNickname()));
        }
    }

    private void savePendingCandidateRegistration(Member member) {
        CandidateRegistration registration = CandidateRegistration.apply(member);
        candidateJpaRepository.save(registration);

        paymentJpaRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, registration.getId(), 1));
    }

    private void saveRejectedCandidateRegistration(Member member, String reason, LocalDateTime now) {
        CandidateRegistration registration = CandidateRegistration.apply(member);
        registration.reject(reason, now.minusDays(7));
        candidateJpaRepository.save(registration);

        Payment payment = Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, registration.getId(), 1);
        payment.reject(now.minusDays(7));
        paymentJpaRepository.save(payment);
    }

    private void saveWithdrawnCandidateRegistration(Member member, LocalDateTime now) {
        CandidateRegistration registration = CandidateRegistration.apply(member);
        registration.approve(now.minusDays(15));
        registration.withdraw(now.minusDays(8));
        candidateJpaRepository.save(registration);

        // 철회 전 결제는 이미 완료된 상태로 기록
        Payment payment = Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, registration.getId(), 1);
        payment.confirm(now.minusDays(15));
        paymentJpaRepository.save(payment);
    }

    private void saveMatchingResults(MatchingApplication application, List<Member> candidates) {
        for (Member candidate : candidates) {
            matchingResultJpaRepository.save(MatchingResult.create(application, candidate));
        }
    }

    private void saveBankAccount(Member member, String bankName, String accountNumber) {
        BankAccount bankAccount = BankAccount.register(
            member.getId(),
            bankName,
            accountNumber,
            member.getLegalName()
        );
        bankAccountJpaRepository.save(bankAccount);
    }

    private Member createMember(String email, String legalName, Gender gender, Mbti mbti, String instagramId) {
        return Member.create(
            email,
            RAW_PASSWORD,
            passwordEncoder,
            legalName,
            gender,
            mbti,
            instagramId,
            "안녕하세요, " + legalName + "입니다.",
            "성격 좋은 분이었으면 합니다."
        );
    }

    private boolean alreadyInitialized() {
        return memberJpaRepository.existsByEmail_AddressAndStatus("202100001@sangmyung.kr", EntityStatus.ACTIVE);
    }

    private void logSummary() {
        log.info("[LocalDataInitializer] 시드 데이터 생성 완료");
        log.info("─────────────────────────────────────────────────────────");
        log.info("  [관리자]");
        log.info("    202100001@sangmyung.kr / {}", RAW_PASSWORD);
        log.info("  [후보자 - 승인됨 (APPROVED, 결제 COMPLETED)]");
        log.info("    남: 202200001, 202200002, 202300001 @sangmyung.kr");
        log.info("    여: 202200101, 202200102, 202300101 @sangmyung.kr");
        log.info("  [후보자 신청 - 대기중 (PENDING, 결제 PENDING)]");
        log.info("    남: 202201001@sangmyung.kr");
        log.info("    여: 202201101@sangmyung.kr");
        log.info("  [후보자 신청 - 거절됨 (REJECTED, 결제 REJECTED)]");
        log.info("    남: 202201002@sangmyung.kr");
        log.info("    여: 202201102@sangmyung.kr");
        log.info("  [후보자 신청 - 철회됨 (WITHDRAWN, 결제 COMPLETED)]");
        log.info("    남: 202201003@sangmyung.kr");
        log.info("    여: 202201103@sangmyung.kr");
        log.info("  [일반 회원]");
        log.info("    남: 202400001, 202400002, 202400003 @sangmyung.kr");
        log.info("    여: 202400101, 202400102, 202400103 @sangmyung.kr");
        log.info("  [매칭 신청]");
        log.info("    정태양(남) - RANDOM 3명 APPROVED + 결과 있음 (결제 COMPLETED)");
        log.info("    정태양(남) - IDEAL  1명 REJECTED             (결제 REJECTED)");
        log.info("    정태양(남) - RANDOM 2명 PENDING              (결제 PENDING)");
        log.info("    홍미래(여) - IDEAL  2명 APPROVED + 결과 있음 (결제 COMPLETED)");
        log.info("    배준혁(남) - RANDOM 2명 PENDING              (결제 PENDING)");
        log.info("    이지혜(여) - IDEAL  1명 REJECTED             (결제 REJECTED)");
        log.info("    신민호(남) - RANDOM 1명 WITHDRAWN            (결제 PENDING)");
        log.info("    김수빈(여) - IDEAL  3명 PENDING              (결제 PENDING)");
        log.info("  [공지사항] 3건");
        log.info("  모든 계정 비밀번호: {}", RAW_PASSWORD);
        log.info("─────────────────────────────────────────────────────────");
    }

}
