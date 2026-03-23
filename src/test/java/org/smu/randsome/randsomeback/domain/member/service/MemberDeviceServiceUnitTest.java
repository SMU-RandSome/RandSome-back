package org.smu.randsome.randsomeback.domain.member.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.implement.MemberDeviceManager;

class MemberDeviceServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberDeviceService memberService;

    @Mock
    MemberDeviceManager memberDeviceManager;

    @Test
    void 디바이스_토큰_동기화에_성공한다() {
        // when
        memberService.syncDeviceTokens(1L, "fcm_device_token_12345");

        // then
        verify(memberDeviceManager).syncDeviceToken(eq(1L), eq("fcm_device_token_12345"), any());
    }

}