package org.smu.randsome.randsomeback.admin.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.admin.member.controller.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberAdminService {

    private final MemberReader memberReader;

    @Transactional(readOnly = true)
    public Page<MemberAdminResponse> getMembers(Pageable pageable) {
        return memberReader.findAll(pageable)
                .map(MemberAdminResponse::from);
    }

}
