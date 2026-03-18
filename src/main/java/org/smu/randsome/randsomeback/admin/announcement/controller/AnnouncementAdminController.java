package org.smu.randsome.randsomeback.admin.announcement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.announcement.controller.dto.request.AnnouncementRegisterRequest;
import org.smu.randsome.randsomeback.admin.announcement.service.AnnouncementAdminService;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AnnouncementAdminController extends AnnouncementAdminControllerDocs {

    private final AnnouncementAdminService announcementAdminService;

    @Override
    @PostMapping("/v1/admin/announcements")
    public ResponseEntity<ApiResponse<Long>> registerAnnouncement(
            @RequestBody @Valid AnnouncementRegisterRequest request,
            @LoginMember Long adminId
    ) {
        Announcement announcement = announcementAdminService.registerAnnouncement(adminId, request.toNewAnnouncement());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(announcement.getId()));
    }

}