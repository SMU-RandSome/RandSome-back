package org.smu.randsome.randsomeback.admin.matching.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.matching.dto.response.AdminMatchingItem;
import org.smu.randsome.randsomeback.admin.matching.service.AdminMatchingService;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingSortType;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AdminMatchingController extends AdminMatchingControllerDocs {

    private final AdminMatchingService adminMatchingService;

    @Override
    @GetMapping("/v1/admin/matching-applications")
    public ApiResponse<PageResponse<AdminMatchingItem>> findMatchings(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") MatchingSortType sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<MatchingApplication> response = adminMatchingService.findMatchings(
                new MatchingSearchCondition(date, gender, keyword, sort),
                new OffsetLimit(page, size)
        );

        return ApiResponse.success(response.map(AdminMatchingItem::from));
    }

}
