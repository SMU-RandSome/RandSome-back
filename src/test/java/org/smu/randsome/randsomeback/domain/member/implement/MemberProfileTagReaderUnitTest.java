package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;

class MemberProfileTagReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberProfileTagReader memberProfileTagReader;

    @Mock
    MemberProfileTagJpaRepository memberProfileTagJpaRepository;

    @Mock
    MemberProfileTagCacheManager cacheManager;

    private static final ProfileTags SAMPLE_TAGS =
            new ProfileTags(PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.ROMANTIC);

    @Test
    void find_캐시_히트_시_DB를_호출하지_않는다() {
        // given
        given(cacheManager.get(1L)).willReturn(Optional.of(SAMPLE_TAGS));

        // when
        ProfileTags result = memberProfileTagReader.find(1L);

        // then
        assertThat(result.personalityTag()).isEqualTo(PersonalityTag.ACTIVE);
        verify(memberProfileTagJpaRepository, never()).findByMemberId(any());
    }

    @Test
    void find_캐시_미스_시_DB에서_조회하고_캐시에_저장한다() {
        // given
        given(cacheManager.get(1L)).willReturn(Optional.empty());

        MemberProfileTag tag = createSimpleTag();
        given(memberProfileTagJpaRepository.findByMemberId(1L)).willReturn(Optional.of(tag));

        // when
        ProfileTags result = memberProfileTagReader.find(1L);

        // then
        assertThat(result.personalityTag()).isEqualTo(PersonalityTag.QUIET);
        verify(memberProfileTagJpaRepository).findByMemberId(1L);
        verify(cacheManager).put(eq(1L), any(ProfileTags.class));
    }

    @Test
    void find_캐시_미스_시_DB에도_없으면_예외가_발생한다() {
        // given
        given(cacheManager.get(1L)).willReturn(Optional.empty());
        given(memberProfileTagJpaRepository.findByMemberId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberProfileTagReader.find(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void findAllByMemberIds_전체_캐시_히트_시_DB를_호출하지_않는다() {
        // given
        List<Long> memberIds = List.of(1L, 2L);
        given(cacheManager.getAll(eq(memberIds), anyMap())).willReturn(List.of());

        // when
        Map<Long, ProfileTags> result = memberProfileTagReader.findAllByMemberIds(memberIds);

        // then
        verify(memberProfileTagJpaRepository, never()).findAllByMemberIdIn(any());
    }

    @Test
    void findAllByMemberIds_부분_캐시_히트_시_미스만_DB에서_조회한다() {
        // given
        List<Long> memberIds = List.of(1L, 2L, 3L);
        given(cacheManager.getAll(eq(memberIds), anyMap())).willReturn(new ArrayList<>(List.of(2L)));

        MemberProfileTag dbTag = createTagWithMember(2L);
        given(memberProfileTagJpaRepository.findAllByMemberIdIn(List.of(2L)))
                .willReturn(List.of(dbTag));

        // when
        Map<Long, ProfileTags> result = memberProfileTagReader.findAllByMemberIds(memberIds);

        // then
        assertThat(result).hasSize(1);
        verify(memberProfileTagJpaRepository).findAllByMemberIdIn(List.of(2L));
        verify(cacheManager).putAll(anyMap());
    }

    @Test
    void findAllByMemberIds_전체_캐시_미스_시_전체_DB에서_조회한다() {
        // given
        List<Long> memberIds = List.of(1L, 2L);
        given(cacheManager.getAll(eq(memberIds), anyMap())).willReturn(new ArrayList<>(memberIds));

        MemberProfileTag tag1 = createTagWithMember(1L);
        MemberProfileTag tag2 = createTagWithMember(2L);
        given(memberProfileTagJpaRepository.findAllByMemberIdIn(memberIds))
                .willReturn(List.of(tag1, tag2));

        // when
        Map<Long, ProfileTags> result = memberProfileTagReader.findAllByMemberIds(memberIds);

        // then
        assertThat(result).hasSize(2);
        verify(memberProfileTagJpaRepository).findAllByMemberIdIn(memberIds);
        verify(cacheManager).putAll(anyMap());
    }

    private MemberProfileTag createSimpleTag() {
        MemberProfileTag tag = org.mockito.Mockito.mock(MemberProfileTag.class);
        org.mockito.Mockito.when(tag.getPersonalityTag()).thenReturn(PersonalityTag.QUIET);
        org.mockito.Mockito.when(tag.getFaceTypeTag()).thenReturn(FaceTypeTag.PUPPY);
        org.mockito.Mockito.when(tag.getDatingStyleTag()).thenReturn(DatingStyleTag.ROMANTIC);
        return tag;
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
