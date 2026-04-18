package org.smu.randsome.randsomeback.domain.report.implement;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.domain.report.repository.ReportJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

@RequiredArgsConstructor
class ReportManagerIntegrationTest extends IntegrationTestSupport {

    final ReportManager reportManager;
    final MemberJpaRepository memberJpaRepository;
    final ReportJpaRepository reportJpaRepository;

    @AfterEach
    void tearDown() {
        reportJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 신고_처리_완료_시_상태가_RESOLVED로_변경되어_DB에_반영된다() {
        // given
        var reporter = memberJpaRepository.save(MemberFixture.createWithGender("202300001@sangmyung.kr", Gender.MALE));
        var reported = memberJpaRepository.save(MemberFixture.createWithGender("202300002@sangmyung.kr", Gender.FEMALE));
        var report = reportJpaRepository.save(
                Report.create(reporter, reported, ReportTargetType.MATCHING_RESULT, 1L,
                        ReportReason.INAPPROPRIATE_CONTENT, "부적절합니다."));

        // when
        reportManager.markAsResolved(report.getId());

        // then
        var updated = reportJpaRepository.findById(report.getId()).orElseThrow();
        assertThat(updated.getReportStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 신고_기각_시_상태가_REJECTED로_변경되어_DB에_반영된다() {
        // given
        var reporter = memberJpaRepository.save(MemberFixture.createWithGender("202300003@sangmyung.kr", Gender.MALE));
        var reported = memberJpaRepository.save(MemberFixture.createWithGender("202300004@sangmyung.kr", Gender.FEMALE));
        var report = reportJpaRepository.save(
                Report.create(reporter, reported, ReportTargetType.MATCHING_RESULT, 1L,
                        ReportReason.INAPPROPRIATE_CONTENT, "부적절합니다."));

        // when
        reportManager.markAsRejected(report.getId());

        // then
        var updated = reportJpaRepository.findById(report.getId()).orElseThrow();
        assertThat(updated.getReportStatus()).isEqualTo(ReportStatus.REJECTED);
    }

}
