package org.smu.randsome.randsomeback.domain.member.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.implement.MemberDeviceManager;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberDeviceService {

    private final MemberDeviceManager memberDeviceManager;

    /**
     * 회원 디바이스 토큰 동기화
     * @param memberId 회원 식별자
     * @param deviceToken 디바이스 토큰
     *
     **/
    public void syncDeviceTokens(Long memberId, String deviceToken) {
        memberDeviceManager.syncDeviceToken(memberId, deviceToken, LocalDateTime.now());

        log.info("[MemberService] 디바이스 토큰 동기화 완료 - memberId: {}", memberId);
    }

    /**
     * 회원 디바이스 토큰 삭제
     * @param memberId 회원 식별자
     * @param deviceToken 디바이스 토큰
     * */
    public void deleteDeviceToken(Long memberId, String deviceToken) {
        memberDeviceManager.deleteDeviceToken(memberId, deviceToken);

        log.info("[MemberService] 디바이스 토큰 삭제 완료 - memberId: {}", memberId);
    }

}