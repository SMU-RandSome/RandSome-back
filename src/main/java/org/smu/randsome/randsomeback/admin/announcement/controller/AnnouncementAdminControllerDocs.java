package org.smu.randsome.randsomeback.admin.announcement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.admin.announcement.dto.request.AnnouncementRegisterRequest;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 공지사항 API", description = "관리자 공지사항 관련 API")
public abstract class AnnouncementAdminControllerDocs {

    @Operation(summary = "공지사항 등록", description = "관리자가 새로운 공지사항을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "공지사항이 성공적으로 등록되었습니다.")
    public abstract ApiResponse<Long> registerAnnouncement(
            @RequestBody @Valid AnnouncementRegisterRequest request,
            @LoginMember Long adminId
    );

}
