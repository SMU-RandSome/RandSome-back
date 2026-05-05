package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberStatisticsReader {

    private final MemberJpaRepository memberJpaRepository;

    public List<CandidateGenderCountItem> findGenderCountByRole(Role role) {
        return memberJpaRepository.findAllGenderCountBy(role, EntityStatus.ACTIVE);
    }

}
