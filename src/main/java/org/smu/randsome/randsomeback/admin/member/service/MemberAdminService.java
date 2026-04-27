package org.smu.randsome.randsomeback.admin.member.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberAdminService {

    private final MemberReader memberReader;
    private final MemberManager memberManager;

    @Transactional(readOnly = true)
    public Page<MemberAdminResponse> findMembers(Pageable pageable) {
        return memberReader.findAll(pageable)
                .map(MemberAdminResponse::from);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse findMemberDetail(Long memberId) {
        Member member = memberReader.find(memberId);

        return MemberDetailResponse.of(member);
    }

    public void suspendMember(Long memberId, String reason) {
        memberManager.suspend(memberId, reason);
    }

    public void restoreMember(Long memberId) {
        memberManager.restore(memberId);
    }

}