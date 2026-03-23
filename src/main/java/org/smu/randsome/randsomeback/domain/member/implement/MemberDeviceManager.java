package org.smu.randsome.randsomeback.domain.member.implement;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.repository.MemberDeviceJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberDeviceManager {

    private final MemberReader memberReader;
    private final MemberDeviceJpaRepository memberDeviceJpaRepository;

    @Transactional
    public void syncDeviceToken(Long memberId, String deviceToken, LocalDateTime now) {
        memberDeviceJpaRepository.findByMemberIdAndDeviceTokenAndStatus(memberId, deviceToken, EntityStatus.ACTIVE)
                .ifPresentOrElse(
                        memberDevice -> memberDevice.updateLastSyncedAt(now),
                        () -> registerNewDeviceToken(memberId, deviceToken, now)
                );
    }

    @Transactional
    public void deleteDeviceToken( Long memberId, String deviceToken) {
        MemberDevice memberDevice = memberDeviceJpaRepository.findByMemberIdAndDeviceTokenAndStatus(
                memberId,
                deviceToken,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_FCM_TOKEN));

        memberDevice.delete();
    }

    private void registerNewDeviceToken(Long memberId, String deviceToken, LocalDateTime now) {
        Member member = memberReader.find(memberId);
        memberDeviceJpaRepository.save(MemberDevice.register(
                member,
                deviceToken,
                now
        ));

        log.info("[MemberDeviceManager] 새로운 디바이스 토큰 등록 memberId: {}", memberId);
    }

}