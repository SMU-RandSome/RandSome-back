package org.smu.randsome.randsomeback.infrastructure.slack;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.smu.randsome.randsomeback.IntegrationTestSupport;

class SlackNotificationSenderIntegrationTest extends IntegrationTestSupport {

    final SlackNotificationSender sender;

    SlackNotificationSenderIntegrationTest(SlackNotificationSender sender) {
        this.sender = sender;
    }

    @Disabled
    @Test
    void 실제_Slack에_알림이_전송된다() {
        // given
        MDC.put("traceId", "real-test-trace-123");
        MDC.put("httpMethod", "POST");
        MDC.put("requestUri", "/test/slack");
        MDC.put("clientIp", "127.0.0.1");

        // when & then
        assertThatNoException().isThrownBy(
                () -> sender.sendErrorNotification("[테스트] 실제 Slack 전송 확인용 메시지입니다.",
                        new RuntimeException("테스트 예외"))
        );

        MDC.clear();
    }

}
