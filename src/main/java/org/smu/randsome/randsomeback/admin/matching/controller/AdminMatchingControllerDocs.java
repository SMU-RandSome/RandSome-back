package org.smu.randsome.randsomeback.admin.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.smu.randsome.randsomeback.admin.matching.dto.response.AdminMatchingItem;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingSortType;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;

@Tag(name = "관리자 매칭 관리 API")
public abstract class AdminMatchingControllerDocs {

    @Operation(summary = "매칭 신청 목록 조회", description = """
            관리자가 매칭 신청 목록을 조회하는 API입니다.
            - `date`: 특정 날짜로 필터링합니다. 미입력 시 전체 날짜 조회합니다.
            - `gender`: 신청자 성별로 필터링합니다. 미입력 시 전체 성별 조회합니다.
            - `keyword`: 신청자 닉네임 또는 법정 이름으로 검색합니다.
            - `sort`: 정렬 조건입니다. `LATEST`(최신순), `OLDEST`(오래된순)를 선택할 수 있으며 기본값은 `LATEST`입니다.
            - `page`와 `size`는 offset 기반 페이지네이션 파라미터입니다.
            """)
    public abstract ApiResponse<PageResponse<AdminMatchingItem>> findMatchings(
            @Parameter(description = "신청 날짜로 필터링", in = ParameterIn.QUERY)
            LocalDate date,
            @Parameter(description = "신청자 성별로 필터링", in = ParameterIn.QUERY)
            Gender gender,
            @Parameter(description = "신청자 닉네임 또는 법정 이름으로 검색", in = ParameterIn.QUERY)
            String keyword,
            @Parameter(description = "정렬 조건 (LATEST: 최신순, OLDEST: 오래된순)", in = ParameterIn.QUERY)
            MatchingSortType sort,
            @Parameter(description = "페이지 번호", in = ParameterIn.QUERY)
            int page,
            @Parameter(description = "페이지 크기", in = ParameterIn.QUERY)
            int size
    );

}
