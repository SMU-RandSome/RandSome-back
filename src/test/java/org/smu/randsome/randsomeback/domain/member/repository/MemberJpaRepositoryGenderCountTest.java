package org.smu.randsome.randsomeback.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MemberJpaRepositoryGenderCountTest extends IntegrationTestSupport {

    final MemberJpaRepository memberJpaRepository;

    @Test
    void 후보자_역할의_성별_카운트를_정확히_반환한다() {
        // given
        memberJpaRepository.saveAll(List.of(
                MemberFixture.createCandidate("202300001@sangmyung.kr", Gender.MALE),
                MemberFixture.createCandidate("202300002@sangmyung.kr", Gender.MALE),
                MemberFixture.createCandidate("202300003@sangmyung.kr", Gender.MALE),
                MemberFixture.createCandidate("202300004@sangmyung.kr", Gender.FEMALE),
                MemberFixture.createCandidate("202300005@sangmyung.kr", Gender.FEMALE)
        ));

        // when
        List<CandidateGenderCountItem> result =
                memberJpaRepository.findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);

        // then
        assertThat(result)
                .extracting(CandidateGenderCountItem::gender, CandidateGenderCountItem::count)
                .containsExactlyInAnyOrder(
                        tuple(Gender.MALE, 3L),
                        tuple(Gender.FEMALE, 2L)
                );
    }

    @Test
    void 후보자가_아닌_일반_회원은_카운트에_포함되지_않는다() {
        // given: ROLE_MEMBER인 회원만 저장
        memberJpaRepository.save(MemberFixture.createWithGender("202300010@sangmyung.kr", Gender.MALE));

        // when
        List<CandidateGenderCountItem> result =
                memberJpaRepository.findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 소프트_삭제된_후보자는_카운트에_포함되지_않는다() {
        // given
        Member deleted = MemberFixture.createCandidate("202300020@sangmyung.kr", Gender.MALE);
        deleted.delete();
        memberJpaRepository.save(deleted);

        memberJpaRepository.save(MemberFixture.createCandidate("202300021@sangmyung.kr", Gender.FEMALE));

        // when
        List<CandidateGenderCountItem> result =
                memberJpaRepository.findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting(CandidateGenderCountItem::gender, CandidateGenderCountItem::count)
                .containsExactly(tuple(Gender.FEMALE, 1L));
    }

    @Test
    void 후보자가_없으면_빈_리스트를_반환한다() {
        // when
        List<CandidateGenderCountItem> result =
                memberJpaRepository.findAllGenderCountBy(Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

}
