package org.smu.randsome.randsomeback.domain.report.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {

    INAPPROPRIATE_CONTENT("부적절한 내용"),
    PLAGIARIZED_PROFILE("도용된 프로필"),
    FAKE_PROFILE("허위 프로필"),
    HARASSMENT("성희롱/괴롭힘"),
    SCAM("사기"),
    OTHER("기타");

    private final String displayName;
}
