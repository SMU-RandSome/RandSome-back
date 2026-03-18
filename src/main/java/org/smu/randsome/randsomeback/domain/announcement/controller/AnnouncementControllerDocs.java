package org.smu.randsome.randsomeback.domain.announcement.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.smu.randsome.randsomeback.domain.announcement.controller.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Announcement", description = "공지사항 API")
public abstract class AnnouncementControllerDocs {

    @Schema(name = "공지사항 조회 API - JWT [O]", description = "현재 활성화된 공지사항 목록을 조회하는 API")
    public abstract ResponseEntity<ApiResponse<List<AnnouncementItem>>> findAnnouncements();

}