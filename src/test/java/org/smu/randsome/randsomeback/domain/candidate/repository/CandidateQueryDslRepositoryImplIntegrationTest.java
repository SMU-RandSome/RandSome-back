package org.smu.randsome.randsomeback.domain.candidate.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class CandidateQueryDslRepositoryImplIntegrationTest extends IntegrationTestSupport {

    final CandidateRepository candidateRepository;
    final MemberJpaRepository memberJpaRepository;

    @Test
    void PENDING_필터로_조회하면_PENDING_상태의_등록만_반환된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "김일번");
        var member2 = saveMember("202221034@sangmyung.kr", "김이번");
        var member3 = saveMember("202221035@sangmyung.kr", "김삼번");

        savePending(member1);
        savePending(member2);

        var rejected = CandidateRegistration.apply(member3);
        rejected.reject("자격 미달", LocalDateTime.now());
        candidateRepository.save(rejected);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CandidateRegistration::getRegistrationStatus)
                .containsOnly(RegistrationStatus.PENDING);
    }

    @Test
    void COMPLETED_필터로_조회하면_처리된_상태의_등록만_반환된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "김일번");
        var member2 = saveMember("202221034@sangmyung.kr", "김이번");
        var member3 = saveMember("202221035@sangmyung.kr", "김삼번");

        savePending(member1); // PENDING — 제외 대상

        var approved = CandidateRegistration.apply(member2);
        approved.approve(LocalDateTime.now());
        candidateRepository.save(approved);

        var rejected = CandidateRegistration.apply(member3);
        rejected.reject("자격 미달", LocalDateTime.now());
        candidateRepository.save(rejected);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.COMPLETED, null);

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CandidateRegistration::getRegistrationStatus)
                .containsExactlyInAnyOrder(RegistrationStatus.APPROVED, RegistrationStatus.REJECTED);
    }

    @Test
    void legalName_키워드로_검색하면_일치하는_회원의_등록만_반환된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "홍길동");
        var member2 = saveMember("202221034@sangmyung.kr", "김철수");

        savePending(member1);
        savePending(member2);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, "홍길");

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getLegalName()).isEqualTo("홍길동");
    }

    @Test
    void nickname_키워드로_검색하면_일치하는_회원의_등록만_반환된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "홍길동");
        var member2 = saveMember("202221034@sangmyung.kr", "김철수");

        savePending(member1);
        savePending(member2);

        // 닉네임은 UUID 기반 고유값이므로 member1의 닉네임 전체를 키워드로 사용
        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, member1.getNickname());

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getId()).isEqualTo(member1.getId());
    }

    @Test
    void lastId_미만의_등록만_반환된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "김일번");
        var member2 = saveMember("202221034@sangmyung.kr", "김이번");
        var member3 = saveMember("202221035@sangmyung.kr", "김삼번");

        var reg1 = savePending(member1);
        var reg2 = savePending(member2);
        var reg3 = savePending(member3);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);

        // when — reg3.id가 lastId이므로 id < reg3.id 인 reg1, reg2만 반환
        var result = candidateRepository.findAllByFilter(condition, reg3.getId(), 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CandidateRegistration::getId)
                .doesNotContain(reg3.getId());
    }

    @Test
    void 결과가_있을_때_id_내림차순으로_반환된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "김일번");
        var member2 = saveMember("202221034@sangmyung.kr", "김이번");
        var member3 = saveMember("202221035@sangmyung.kr", "김삼번");

        var reg1 = savePending(member1);
        var reg2 = savePending(member2);
        var reg3 = savePending(member3);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).extracting(CandidateRegistration::getId)
                .containsExactly(reg3.getId(), reg2.getId(), reg1.getId());
    }

    @Test
    void size_더하기_1개까지_조회된다() {
        // given — size+1 개를 저장해 limit(size+1)이 꽉 차는지 확인
        int size = 10;
        for (int i = 0; i <= size; i++) {
            var m = saveMember(String.format("%09d@sangmyung.kr", 202221000 + i), "회원" + i);
            savePending(m);
        }

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);

        // when
        var result = candidateRepository.findAllByFilter(condition, null, size);

        // then — QueryDSL이 limit(size+1)로 조회하므로 size+1 건 반환
        assertThat(result).hasSize(size + 1);
    }

    @Test
    void DELETED_상태의_등록은_조회에서_제외된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "김일번");
        var member2 = saveMember("202221034@sangmyung.kr", "김이번");

        savePending(member1);

        var toDelete = savePending(member2);
        toDelete.delete();
        candidateRepository.save(toDelete);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getId()).isEqualTo(member1.getId());
    }

    @Test
    void DELETED_회원의_등록은_조회에서_제외된다() {
        // given
        var member1 = saveMember("202221033@sangmyung.kr", "김일번");
        var member2 = saveMember("202221034@sangmyung.kr", "김이번");

        savePending(member1);
        savePending(member2);

        member2.delete();
        memberJpaRepository.save(member2);

        var condition = new CandidateRegistrationSearchCondition(CandidateRegistrationFilter.PENDING, null);

        // when
        var result = candidateRepository.findAllByFilter(condition, null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getId()).isEqualTo(member1.getId());
    }

    private Member saveMember(String email, String legalName) {
        return memberJpaRepository.save(MemberFixture.createWithLegalName(email, legalName));
    }

    private CandidateRegistration savePending(Member member) {
        return candidateRepository.save(CandidateRegistration.apply(member));
    }

}
