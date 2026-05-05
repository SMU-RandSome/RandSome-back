package org.smu.randsome.randsomeback.domain.announcement.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class AnnouncementReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    AnnouncementReader announcementReader;

    @Mock
    AnnouncementJpaRepository announcementJpaRepository;

    @Mock
    RedisRepository redisRepository;

    @Mock
    ObjectMapper objectMapper;

    @Test
    void 캐시_미스_시_DB를_조회하고_결과를_반환한다() throws Exception {
        // given
        given(redisRepository.get(CacheKeys.ANNOUNCEMENTS)).willReturn(null);
        var admin = mock(Member.class);
        var a1 = Announcement.register(admin, "제목1", "내용1");
        var a2 = Announcement.register(admin, "제목2", "내용2");
        given(announcementJpaRepository.findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE)).willReturn(List.of(a1, a2));
        given(objectMapper.writeValueAsString(any())).willReturn("[]");

        // when
        List<AnnouncementItem> result = announcementReader.findAnnouncements();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("제목1");
        assertThat(result.get(1).title()).isEqualTo("제목2");
        verify(announcementJpaRepository).findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE);
    }

    @Test
    void 캐시_미스_시_DB_조회_결과를_캐시에_저장한다() throws Exception {
        // given
        given(redisRepository.get(CacheKeys.ANNOUNCEMENTS)).willReturn(null);
        given(announcementJpaRepository.findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE)).willReturn(List.of());
        given(objectMapper.writeValueAsString(any())).willReturn("[]");

        // when
        announcementReader.findAnnouncements();

        // then
        verify(redisRepository).put(eq(CacheKeys.ANNOUNCEMENTS), eq("[]"), any(Duration.class));
    }

    @Test
    void 캐시_히트_시_DB를_조회하지_않는다() throws Exception {
        // given
        String cachedJson = "[{\"id\":1,\"title\":\"제목1\",\"content\":\"내용1\"}]";
        given(redisRepository.get(CacheKeys.ANNOUNCEMENTS)).willReturn(cachedJson);
        given(objectMapper.readValue(eq(cachedJson), any(TypeReference.class)))
                .willReturn(List.of(AnnouncementItem.builder().id(1L).title("제목1").content("내용1").build()));

        // when
        List<AnnouncementItem> result = announcementReader.findAnnouncements();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("제목1");
        verify(announcementJpaRepository, never()).findAllByStatusOrderByIdDesc(any());
    }

    @Test
    void 캐시_역직렬화_실패_시_DB를_재조회한다() throws Exception {
        // given — 캐시에 값이 있지만 역직렬화 불가능한 상태
        String malformedJson = "invalid-json";
        given(redisRepository.get(CacheKeys.ANNOUNCEMENTS)).willReturn(malformedJson);
        given(objectMapper.readValue(eq(malformedJson), any(TypeReference.class)))
                .willThrow(new JsonProcessingException("역직렬화 실패") {});
        var admin = mock(Member.class);
        given(announcementJpaRepository.findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE))
                .willReturn(List.of(Announcement.register(admin, "제목", "내용")));
        given(objectMapper.writeValueAsString(any())).willReturn("[]");

        // when
        List<AnnouncementItem> result = announcementReader.findAnnouncements();

        // then — 역직렬화 실패 시 DB 재조회로 폴백한다
        assertThat(result).hasSize(1);
        verify(announcementJpaRepository).findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE);
    }

}
