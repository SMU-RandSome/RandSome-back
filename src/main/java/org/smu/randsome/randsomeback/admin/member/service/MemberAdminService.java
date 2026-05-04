package org.smu.randsome.randsomeback.admin.member.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSearchCondition;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberAdminService {

    private final MemberReader memberReader;
    private final MemberManager memberManager;
    private final CandidateManager candidateManager;

    public PageResponse<Member> findMembers(MemberSearchCondition condition, OffsetLimit offsetLimit) {
        return memberReader.findAll(condition, offsetLimit);
    }

    public MemberDetailResponse findMemberDetail(Long memberId) {
        Member member = memberReader.findNonDeleted(memberId);

        return MemberDetailResponse.of(member);
    }

    @Transactional
    public void suspendMember(Long memberId, String reason) {
        memberManager.suspend(memberId, reason);
        candidateManager.suspend(memberId);
    }

    public void restoreMember(Long memberId) {
        memberManager.restore(memberId);
    }

}
