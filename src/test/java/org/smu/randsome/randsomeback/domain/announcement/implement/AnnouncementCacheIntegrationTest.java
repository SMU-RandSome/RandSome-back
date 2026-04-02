package org.smu.randsome.randsomeback.domain.announcement.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.admin.announcement.service.AnnouncementAdminService;
import org.smu.randsome.randsomeback.domain.announcement.dto.command.NewAnnouncement;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@RequiredArgsConstructor
class AnnouncementCacheIntegrationTest extends IntegrationTestSupport {

    final AnnouncementReader announcementReader;
    final AnnouncementAdminService announcementAdminService;
    final MemberJpaRepository memberJpaRepository;
    final RedisRepository redisRepository;

    @MockitoSpyBean
    AnnouncementJpaRepository announcementJpaRepository;

    @BeforeEach
    void setUp() {
        redisRepository.delete(CacheKeys.ANNOUNCEMENTS);
        announcementJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        redisRepository.delete(CacheKeys.ANNOUNCEMENTS);
        announcementJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 첫_조회_시_캐시_미스가_발생하고_결과가_캐시에_저장된다() {
        // given
        Member admin = saveAdmin();
        announcementJpaRepository.save(Announcement.register(admin, "제목1", "내용1"));

        // when
        List<AnnouncementItem> result = announcementReader.findAnnouncements();

        // then — DB 1회 호출 = 캐시 미스
        assertThat(result).hasSize(1);
        verify(announcementJpaRepository, times(1)).findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE);
    }

    @Test
    void 두_번째_조회_시_캐시_히트가_발생하고_DB를_재조회하지_않는다() {
        // given
        Member admin = saveAdmin();
        announcementJpaRepository.save(Announcement.register(admin, "제목1", "내용1"));

        announcementReader.findAnnouncements(); // 캐시 워밍 (DB 1회 호출)

        // when
        List<AnnouncementItem> result = announcementReader.findAnnouncements();

        // then — DB 총 1회만 호출됨 = 두 번째 조회는 캐시 히트
        assertThat(result).hasSize(1);
        verify(announcementJpaRepository, times(1)).findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE);
    }

    @Test
    void 공지사항_등록_후_캐시가_무효화되어_다음_조회_시_새_데이터가_반환된다() {
        // given
        Member admin = saveAdmin();
        announcementJpaRepository.save(Announcement.register(admin, "기존 공지", "기존 내용"));

        List<AnnouncementItem> before = announcementReader.findAnnouncements();
        assertThat(before).hasSize(1);

        // when
        announcementAdminService.registerAnnouncement(admin.getId(),
                NewAnnouncement.builder()
                        .title("새 공지")
                        .content("새 내용")
                        .build());

        // then — 캐시 evict 후 재조회
        List<AnnouncementItem> after = announcementReader.findAnnouncements();
        assertThat(after).hasSize(2);
    }

    private Member saveAdmin() {
        Member member = MemberFixture.create();
        member.updateRole(Role.ROLE_ADMIN);
        return memberJpaRepository.save(member);
    }

}
