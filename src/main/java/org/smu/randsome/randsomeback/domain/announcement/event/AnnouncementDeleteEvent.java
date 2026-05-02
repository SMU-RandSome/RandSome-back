package org.smu.randsome.randsomeback.domain.announcement.event;

/**
 * 공지사항 삭제 완료 후 발행되는 도메인 이벤트.
 */
public record AnnouncementDeleteEvent(Long announcementId) {

}