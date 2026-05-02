package org.smu.randsome.randsomeback.domain.statistics.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@RequiredArgsConstructor
class MatchingStatsCacheIntegrationTest extends IntegrationTestSupport {

    final MatchingStatsReader matchingStatsReader;
    final MemberJpaRepository memberJpaRepository;
    final RedisRepository redisRepository;

    @MockitoSpyBean
    MatchingJpaRepository matchingJpaRepository;

    @BeforeEach
    void setUp() {
        redisRepository.delete(CacheKeys.MATCHING_TOTAL_COUNT);
        matchingJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        redisRepository.delete(CacheKeys.MATCHING_TOTAL_COUNT);
        matchingJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 첫_조회_시_캐시_미스가_발생하고_결과가_캐시에_저장된다() {
        // given
        Member member = memberJpaRepository.save(MemberFixture.create());
        matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));

        // when
        long count = matchingStatsReader.countTotal();

        // then
        assertThat(count).isEqualTo(1);
        verify(matchingJpaRepository, times(1)).countByStatus(EntityStatus.ACTIVE);

        String cached = redisRepository.get(CacheKeys.MATCHING_TOTAL_COUNT);
        assertThat(cached).isEqualTo("1");
    }

    @Test
    void 두_번째_조회_시_캐시_히트가_발생하고_DB를_재조회하지_않는다() {
        // given
        Member member = memberJpaRepository.save(MemberFixture.create());
        matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));

        matchingStatsReader.countTotal(); // 캐시 워밍

        // when
        long count = matchingStatsReader.countTotal();

        // then
        assertThat(count).isEqualTo(1);
        verify(matchingJpaRepository, times(1)).countByStatus(EntityStatus.ACTIVE);
    }

}
