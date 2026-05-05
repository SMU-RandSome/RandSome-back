package org.smu.randsome.randsomeback.domain.matching.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingSortType;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingQueryDslRepositoryIntegrationTest extends IntegrationTestSupport {

    final MemberJpaRepository memberJpaRepository;
    final MatchingRepository matchingRepository;

    // ===== 정렬 =====

    @Test
    void 최신순_정렬이면_ID_내림차순으로_반환된다() {
        var member = save(MemberFixture.create());
        var a1 = matchingRepository.save(application(member));
        var a2 = matchingRepository.save(application(member));
        var a3 = matchingRepository.save(application(member));

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result)
                .extracting(MatchingApplication::getId)
                .containsExactly(a3.getId(), a2.getId(), a1.getId());
    }

    @Test
    void 오래된순_정렬이면_ID_오름차순으로_반환된다() {
        var member = save(MemberFixture.create());
        var a1 = matchingRepository.save(application(member));
        var a2 = matchingRepository.save(application(member));
        var a3 = matchingRepository.save(application(member));

        var condition = condition(null, null, null, MatchingSortType.OLDEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result)
                .extracting(MatchingApplication::getId)
                .containsExactly(a1.getId(), a2.getId(), a3.getId());
    }

    // ===== 성별 필터 =====

    @Test
    void 성별_필터가_MALE이면_남성_신청자만_반환된다() {
        var male = save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var female = save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.FEMALE));
        matchingRepository.save(application(male));
        matchingRepository.save(application(female));

        var condition = condition(null, Gender.MALE, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getGender()).isEqualTo(Gender.MALE);
    }

    @Test
    void 성별_필터가_null이면_전체_성별을_반환한다() {
        var male = save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var female = save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.FEMALE));
        matchingRepository.save(application(male));
        matchingRepository.save(application(female));

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(2);
    }

    // ===== 날짜 필터 =====

    @Test
    void 오늘_날짜로_필터링하면_오늘_신청만_반환된다() {
        var member = save(MemberFixture.create());
        matchingRepository.save(application(member));
        matchingRepository.save(application(member));

        var condition = condition(LocalDate.now(), null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(2);
    }

    @Test
    void 어제_날짜로_필터링하면_결과가_없다() {
        var member = save(MemberFixture.create());
        matchingRepository.save(application(member));

        var condition = condition(LocalDate.now().minusDays(1), null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).isEmpty();
    }

    // ===== 키워드 필터 (법정이름) =====

    @Test
    void 법정이름으로_검색하면_해당_이름이_포함된_신청만_반환된다() {
        var hong = save(MemberFixture.createWithLegalName("202310001@sangmyung.kr", "홍길동"));
        var kim = save(MemberFixture.createWithLegalName("202310002@sangmyung.kr", "김철수"));
        matchingRepository.save(application(hong));
        matchingRepository.save(application(kim));

        var condition = condition(null, null, "홍길동", MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getLegalName()).isEqualTo("홍길동");
    }

    @Test
    void 키워드가_null이면_전체_조회한다() {
        var m1 = save(MemberFixture.createWithLegalName("202310001@sangmyung.kr", "홍길동"));
        var m2 = save(MemberFixture.createWithLegalName("202310002@sangmyung.kr", "김철수"));
        matchingRepository.save(application(m1));
        matchingRepository.save(application(m2));

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(2);
    }

    @Test
    void 공백_키워드는_전체_조회와_동일하다() {
        var member = save(MemberFixture.create());
        matchingRepository.save(application(member));

        var condition = condition(null, null, "   ", MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(1);
    }

    // ===== 페이지네이션 =====

    @Test
    void offset_0_limit_2이면_첫_두_건을_반환한다() {
        var member = save(MemberFixture.create());
        matchingRepository.save(application(member));
        matchingRepository.save(application(member));
        matchingRepository.save(application(member));
        matchingRepository.save(application(member));

        var condition = condition(null, null, null, MatchingSortType.OLDEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 2);

        assertThat(result).hasSize(2);
    }

    @Test
    void offset_2_limit_2이면_두_번째_페이지를_반환한다() {
        var member = save(MemberFixture.create());
        var a1 = matchingRepository.save(application(member));
        var a2 = matchingRepository.save(application(member));
        var a3 = matchingRepository.save(application(member));
        var a4 = matchingRepository.save(application(member));

        var condition = condition(null, null, null, MatchingSortType.OLDEST);
        List<MatchingApplication> page1 = matchingRepository.findAllByFilter(condition, 0, 2);
        List<MatchingApplication> page2 = matchingRepository.findAllByFilter(condition, 2, 2);

        assertThat(page1).extracting(MatchingApplication::getId)
                .containsExactly(a1.getId(), a2.getId());
        assertThat(page2).extracting(MatchingApplication::getId)
                .containsExactly(a3.getId(), a4.getId());
    }

    // ===== countByFilter =====

    @Test
    void 전체_count를_반환한다() {
        var member = save(MemberFixture.create());
        matchingRepository.save(application(member));
        matchingRepository.save(application(member));
        matchingRepository.save(application(member));

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        long count = matchingRepository.countByFilter(condition);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void 성별_필터를_적용하면_해당_성별_count만_반환한다() {
        var male = save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var female = save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.FEMALE));
        matchingRepository.save(application(male));
        matchingRepository.save(application(male));
        matchingRepository.save(application(female));

        var condition = condition(null, Gender.MALE, null, MatchingSortType.LATEST);
        long count = matchingRepository.countByFilter(condition);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void 결과가_없으면_count가_0이다() {
        var condition = condition(null, null, "존재하지않는이름", MatchingSortType.LATEST);
        long count = matchingRepository.countByFilter(condition);

        assertThat(count).isZero();
    }

    // ===== 소프트 삭제 =====

    @Test
    void 삭제된_신청은_조회되지_않는다() {
        var member = save(MemberFixture.create());
        var active = matchingRepository.save(application(member));
        var deleted = matchingRepository.save(application(member));
        deleted.delete();

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(active.getId());
    }

    @Test
    void 삭제된_신청은_count에_포함되지_않는다() {
        var member = save(MemberFixture.create());
        matchingRepository.save(application(member));
        var deleted = matchingRepository.save(application(member));
        deleted.delete();

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        long count = matchingRepository.countByFilter(condition);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void 탈퇴한_회원의_신청은_조회되지_않는다() {
        var active = save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var withdrawn = save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.MALE));
        matchingRepository.save(application(active));
        matchingRepository.save(application(withdrawn));
        withdrawn.delete();

        var condition = condition(null, null, null, MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getId()).isEqualTo(active.getId());
    }

    // ===== 복합 필터 =====

    @Test
    void 성별과_키워드를_함께_적용하면_교집합으로_필터링된다() {
        // createWithLegalName → DEFAULT_GENDER(MALE), createWithGender → DEFAULT_LEGAL_NAME("홍길동")
        var maleHong = save(MemberFixture.createWithLegalName("202310001@sangmyung.kr", "홍길동"));
        var femaleHong = save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.FEMALE));
        matchingRepository.save(application(maleHong));
        matchingRepository.save(application(femaleHong));

        var condition = condition(null, Gender.MALE, "홍", MatchingSortType.LATEST);
        List<MatchingApplication> result = matchingRepository.findAllByFilter(condition, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getGender()).isEqualTo(Gender.MALE);
    }

    // ===== 헬퍼 =====

    private Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    private MatchingApplication application(Member member) {
        return MatchingApplication.apply(member, MatchingType.RANDOM, 1);
    }

    private MatchingSearchCondition condition(LocalDate date, Gender gender, String keyword, MatchingSortType sort) {
        return new MatchingSearchCondition(date, gender, keyword, sort);
    }

}
