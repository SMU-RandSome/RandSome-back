package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class MemberProfileTagReaderUnitTest extends UnitTestSupport {

    MemberProfileTagReader memberProfileTagReader;

    @Mock
    MemberProfileTagJpaRepository memberProfileTagJpaRepository;

    @Mock
    RedisRepository redisRepository;

    @BeforeEach
    void setUp() {
        memberProfileTagReader = new MemberProfileTagReader(
                memberProfileTagJpaRepository, redisRepository, new ObjectMapper());
    }

    private static final String CACHED_JSON =
            "{\"personalityTag\":\"ACTIVE\",\"faceTypeTag\":\"PUPPY\",\"datingStyleTag\":\"ROMANTIC\"}";

    @Test
    void find_캐시_히트_시_DB를_호출하지_않는다() {
        // given
        given(redisRepository.get(any())).willReturn(CACHED_JSON);

        // when
        MemberProfileTag result = memberProfileTagReader.find(1L);

        // then
        assertThat(result.getPersonalityTag()).isEqualTo(PersonalityTag.ACTIVE);
        verify(memberProfileTagJpaRepository, never()).findByMemberId(any());
    }

    @Test
    void find_캐시_미스_시_DB에서_조회하고_캐시에_저장한다() {
        // given
        given(redisRepository.get(any())).willReturn(null);

        MemberProfileTag tag = MemberProfileTag.forCache(
                PersonalityTag.QUIET, FaceTypeTag.PUPPY, DatingStyleTag.ROMANTIC);
        given(memberProfileTagJpaRepository.findByMemberId(1L)).willReturn(Optional.of(tag));

        // when
        MemberProfileTag result = memberProfileTagReader.find(1L);

        // then
        assertThat(result.getPersonalityTag()).isEqualTo(PersonalityTag.QUIET);
        verify(memberProfileTagJpaRepository).findByMemberId(1L);
        verify(redisRepository).put(any(), any(), any());
    }

    @Test
    void find_캐시_미스_시_DB에도_없으면_예외가_발생한다() {
        // given
        given(redisRepository.get(any())).willReturn(null);
        given(memberProfileTagJpaRepository.findByMemberId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberProfileTagReader.find(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void find_Redis_장애_시_DB_fallback한다() {
        // given
        given(redisRepository.get(any())).willThrow(new RuntimeException("Redis 장애"));

        MemberProfileTag tag = MemberProfileTag.forCache(
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.ROMANTIC);
        given(memberProfileTagJpaRepository.findByMemberId(1L)).willReturn(Optional.of(tag));

        // when
        MemberProfileTag result = memberProfileTagReader.find(1L);

        // then
        assertThat(result.getPersonalityTag()).isEqualTo(PersonalityTag.ACTIVE);
        verify(memberProfileTagJpaRepository).findByMemberId(1L);
    }

    @Test
    void findAllByMemberIds_전체_캐시_히트_시_DB를_호출하지_않는다() {
        // given
        List<Long> memberIds = List.of(1L, 2L);
        given(redisRepository.mget(anyList())).willReturn(List.of(CACHED_JSON, CACHED_JSON));

        // when
        Map<Long, MemberProfileTag> result = memberProfileTagReader.findAllByMemberIds(memberIds);

        // then
        assertThat(result).hasSize(2);
        verify(memberProfileTagJpaRepository, never()).findAllByMemberIdIn(any());
    }

    @Test
    void findAllByMemberIds_부분_캐시_히트_시_미스만_DB에서_조회한다() {
        // given
        List<Long> memberIds = List.of(1L, 2L, 3L);
        given(redisRepository.mget(anyList())).willReturn(Arrays.asList(CACHED_JSON, null, CACHED_JSON));

        MemberProfileTag dbTag = createTagWithMember(2L);
        given(memberProfileTagJpaRepository.findAllByMemberIdIn(eq(List.of(2L))))
                .willReturn(List.of(dbTag));

        // when
        Map<Long, MemberProfileTag> result = memberProfileTagReader.findAllByMemberIds(memberIds);

        // then
        assertThat(result).hasSize(3);
        verify(memberProfileTagJpaRepository).findAllByMemberIdIn(List.of(2L));
    }

    @Test
    void findAllByMemberIds_Redis_장애_시_전체_DB_fallback한다() {
        // given
        List<Long> memberIds = List.of(1L, 2L);
        given(redisRepository.mget(anyList())).willThrow(new RuntimeException("Redis 장애"));

        MemberProfileTag tag1 = createTagWithMember(1L);
        MemberProfileTag tag2 = createTagWithMember(2L);
        given(memberProfileTagJpaRepository.findAllByMemberIdIn(memberIds))
                .willReturn(List.of(tag1, tag2));

        // when
        Map<Long, MemberProfileTag> result = memberProfileTagReader.findAllByMemberIds(memberIds);

        // then
        assertThat(result).hasSize(2);
        verify(memberProfileTagJpaRepository).findAllByMemberIdIn(memberIds);
    }

    private MemberProfileTag createTagWithMember(Long memberId) {
        MemberProfileTag tag = org.mockito.Mockito.mock(MemberProfileTag.class);
        org.smu.randsome.randsomeback.domain.member.entity.Member member =
                org.mockito.Mockito.mock(org.smu.randsome.randsomeback.domain.member.entity.Member.class);
        org.mockito.Mockito.when(member.getId()).thenReturn(memberId);
        org.mockito.Mockito.when(tag.getMember()).thenReturn(member);
        org.mockito.Mockito.when(tag.getPersonalityTag()).thenReturn(PersonalityTag.QUIET);
        org.mockito.Mockito.when(tag.getFaceTypeTag()).thenReturn(FaceTypeTag.PUPPY);
        org.mockito.Mockito.when(tag.getDatingStyleTag()).thenReturn(DatingStyleTag.ROMANTIC);
        return tag;
    }

}
