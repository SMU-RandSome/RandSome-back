package org.smu.randsome.randsomeback.domain.announcement.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.announcement.controller.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.service.AnnouncementService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AnnouncementController extends AnnouncementControllerDocs {

    private final AnnouncementService announcementService;

    @Override
    @GetMapping("/v1/announcements")
    public ResponseEntity<ApiResponse<List<AnnouncementItem>>> findAnnouncements() {
        List<Announcement> announcements = announcementService.findAnnouncements();

        List<AnnouncementItem> responses = announcements.stream()
                .map(AnnouncementItem::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

}