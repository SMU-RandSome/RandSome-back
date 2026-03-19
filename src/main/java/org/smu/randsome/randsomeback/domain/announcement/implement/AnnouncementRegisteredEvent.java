package org.smu.randsome.randsomeback.domain.announcement.implement;

/**
 * 공지사항 등록 완료 후 발행되는 도메인 이벤트.
 */
public record AnnouncementRegisteredEvent(Long announcementId) {

}