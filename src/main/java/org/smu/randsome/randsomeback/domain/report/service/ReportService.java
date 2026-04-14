package org.smu.randsome.randsomeback.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.report.dto.command.NewReport;
import org.smu.randsome.randsomeback.domain.report.dto.response.ReportResponse;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.domain.report.implement.ReportManager;
import org.smu.randsome.randsomeback.domain.report.implement.ReportValidator;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportManager reportManager;
    private final ReportValidator reportValidator;
    private final MatchingReader matchingReader;

    @Transactional
    public Long createReport(NewReport newReport, Long reporterId) {
        MatchingResult matchingResult = matchingReader.findMatchingResult(newReport.matchingResultId());

        Member applicant = matchingResult.getMatchingApplication().getMember();
        Member reportedMember = matchingResult.getCandidate();

        reportValidator.validateIsApplicant(applicant.getId(), reporterId);

        reportValidator.validateReportCreation(
                applicant.getId(),
                reportedMember.getId(),
                ReportTargetType.MATCHING_RESULT,
                matchingResult.getId()
        );

        Report savedReport = reportManager.create(
                applicant,
                reportedMember,
                ReportTargetType.MATCHING_RESULT,
                matchingResult.getId(),
                newReport.reason(),
                newReport.description()
        );

        return savedReport.getId();
    }
}
