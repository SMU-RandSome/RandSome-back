package org.smu.randsome.randsomeback.global.support.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    ANNOUNCEMENT_REGISTERED   ("공지사항", "랜섬의 새로운 소식과 업데이트를 확인하세요!"),
    CANDIDATE_APPLIED_TO_ADMIN ("후보자 신청", "새로운 후보자 등록 신청이 있습니다."),
    MATCHING_APPLIED_TO_ADMIN ("매칭 신청", "새로운 랜섬 매칭 신청이 있습니다."),
    MATCHING_APPROVED         ("매칭 승인", "축하합니다! 매칭이 승인되었습니다."),
    MATCHING_REJECTED         ("매칭 거절", "안타깝게도 매칭이 거절되었습니다.");

    private final String title;
    private final String message;

}