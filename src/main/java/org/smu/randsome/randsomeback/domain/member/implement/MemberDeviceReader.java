package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberDeviceJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MemberDeviceReader {

    private final MemberDeviceJpaRepository memberDeviceRepository;

    @Transactional(readOnly = true)
    public List<MemberDevice> findAllActive() {
        return memberDeviceRepository.findAllByStatus(EntityStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<MemberDevice> findAllByAdminRole() {
        return memberDeviceRepository.findAllByAdmin(Role.ROLE_ADMIN, EntityStatus.ACTIVE);
    }

}