package org.smu.randsome.randsomeback.global.config;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.bankaccount.entity.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.repository.BankAccountJpaRepository;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (alreadyInitialized()) {
            log.info("[LocalDataInitializer] 시드 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("[LocalDataInitializer] 로컬 시드 데이터를 생성합니다. (비밀번호: {})", RAW_PASSWORD);

        Member admin = createMember("202100001@sangmyung.kr", "관리자", Gender.MALE, Mbti.ENTJ, "admin_insta");
        admin.updateRole(Role.ROLE_ADMIN);
        memberJpaRepository.save(admin);
        saveBankAccount(admin, "국민", "111111111111");

        List<Member> maleCandidates = List.of(
            createMember("202200001@sangmyung.kr", "김철수", Gender.MALE, Mbti.ISTP, "male_cand_1"),
            createMember("202200002@sangmyung.kr", "박민준", Gender.MALE, Mbti.ENFP, "male_cand_2"),
            createMember("202300001@sangmyung.kr", "이준호", Gender.MALE, Mbti.INTJ, "male_cand_3")
        );

        List<Member> femaleCandidates = List.of(
            createMember("202200101@sangmyung.kr", "김지원", Gender.FEMALE, Mbti.INFJ, "female_cand_1"),
            createMember("202200102@sangmyung.kr", "박서연", Gender.FEMALE, Mbti.ESFJ, "female_cand_2"),
            createMember("202300101@sangmyung.kr", "이수아", Gender.FEMALE, Mbti.ENFJ, "female_cand_3")
        );

        List<Member> regularMembers = List.of(
            createMember("202400001@sangmyung.kr", "정태양", Gender.MALE, Mbti.ESTP, "regular_male_1"),
            createMember("202400002@sangmyung.kr", "홍미래", Gender.FEMALE, Mbti.ISFP, "regular_female_1")
        );

        saveCandidates(maleCandidates);
        saveCandidates(femaleCandidates);
        memberJpaRepository.saveAll(regularMembers);
        regularMembers.forEach(m -> saveBankAccount(m, "신한", "222222222222"));

        log.info("[LocalDataInitializer] 시드 데이터 생성 완료");
        log.info("  관리자    : 202100001@sangmyung.kr / {}", RAW_PASSWORD);
        log.info("  남성 후보자: 202200001~202300001@sangmyung.kr / {}", RAW_PASSWORD);
        log.info("  여성 후보자: 202200101~202300101@sangmyung.kr / {}", RAW_PASSWORD);
        log.info("  일반 회원 : 202400001~202400002@sangmyung.kr / {}", RAW_PASSWORD);
    }

    private void saveCandidates(List<Member> members) {
        for (Member member : members) {
            member.updateRole(Role.ROLE_CANDIDATE);
            memberJpaRepository.save(member);
            saveBankAccount(member, "카카오", "333333333333");

            CandidateRegistration registration = CandidateRegistration.apply(member);
            registration.approve(LocalDateTime.now());
            candidateJpaRepository.save(registration);
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

}