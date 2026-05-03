package org.smu.randsome.randsomeback.domain.matching.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class MatchingResultDetailItemTest extends UnitTestSupport {

    @Test
    void 탈퇴한_후보자의_매칭_결과는_개인정보가_가려지고_withdrawn이_true이다() {
        // given
        Member candidate = MemberFixture.create();
        candidate.withdraw();

        MatchingResult result = mock(MatchingResult.class);
        given(result.getId()).willReturn(1L);
        given(result.getCandidate()).willReturn(candidate);

        // when
        MatchingResultDetailItem item = MatchingResultDetailItem.from(result, null);

        // then
        assertThat(item.withdrawn()).isTrue();
        assertThat(item.nickname()).isEqualTo("탈퇴한 회원");
        assertThat(item.gender()).isNull();
        assertThat(item.mbti()).isNull();
        assertThat(item.instagramId()).isNull();
        assertThat(item.selfIntroduction()).isNull();
        assertThat(item.personalityTag()).isNull();
    }

    @Test
    void 활성_후보자의_매칭_결과는_개인정보가_포함되고_withdrawn이_false이다() {
        // given
        Member candidate = MemberFixture.create();
        ProfileTags profileTag = MemberFixture.createProfileTags();

        MatchingResult result = mock(MatchingResult.class);
        given(result.getId()).willReturn(1L);
        given(result.getCandidate()).willReturn(candidate);

        // when
        MatchingResultDetailItem item = MatchingResultDetailItem.from(result, profileTag);

        // then
        assertThat(item.withdrawn()).isFalse();
        assertThat(item.nickname()).isEqualTo(candidate.getNickname());
        assertThat(item.gender()).isEqualTo(candidate.getGender());
        assertThat(item.mbti()).isEqualTo(candidate.getMbti());
        assertThat(item.instagramId()).isEqualTo(MemberFixture.DEFAULT_INSTAGRAM_ID);
        assertThat(item.personalityTag()).isEqualTo(MemberFixture.DEFAULT_PERSONALITY_TAG);
    }

}
