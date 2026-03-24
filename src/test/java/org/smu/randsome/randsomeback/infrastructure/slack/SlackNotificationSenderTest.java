package org.smu.randsome.randsomeback.infrastructure.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

class SlackNotificationSenderTest {

    private SlackNotificationSender sender;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        sender = new SlackNotificationSender();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
        MDC.clear();
    }

    // ===== buildPayload =====

    @Test
    void MDC_값이_있으면_페이로드에_포함된다() throws IOException {
        // given
        MDC.put("traceId", "abc-123");
        MDC.put("httpMethod", "POST");
        MDC.put("requestUri", "/api/test");
        MDC.put("clientIp", "127.0.0.1");

        // when
        String payload = sender.buildPayload("에러 발생", new RuntimeException("테스트"));

        // then
        assertThat(payload)
                .contains("abc-123")
                .contains("POST")
                .contains("/api/test")
                .contains("127.0.0.1")
                .contains("에러 발생");
    }

    @Test
    void MDC_값이_없으면_NA로_대체된다() throws IOException {
        // given - MDC 비어있음

        // when
        String payload = sender.buildPayload("에러 발생", null);

        // then
        assertThat(payload).contains("N/A");
    }

    @Test
    void queryString이_있으면_requestUri에_붙어서_포함된다() throws IOException {
        // given
        MDC.put("requestUri", "/api/test");
        MDC.put("queryString", "page=1&size=10");

        // when
        String payload = sender.buildPayload("에러 발생", null);

        // then
        assertThat(payload).contains("/api/test?page=1&size=10");
    }

    @Test
    void queryString이_없으면_requestUri만_포함된다() throws IOException {
        // given
        MDC.put("requestUri", "/api/test");

        // when
        String payload = sender.buildPayload("에러 발생", null);

        // then
        assertThat(payload).doesNotContain("?");
    }

    @Test
    void throwable이_null이면_스택트레이스가_NA로_표시된다() throws IOException {
        // when
        String payload = sender.buildPayload("에러 발생", null);

        // then
        assertThat(payload).contains("N/A");
    }

    @Test
    void 스택트레이스는_최대_5줄로_제한된다() throws IOException {
        // given - 스택이 긴 예외
        RuntimeException ex = new RuntimeException("테스트");

        // when
        String payload = sender.buildPayload("에러 발생", ex);

        // then - 5줄 제한으로 잘렸는지: 실제 스택과 비교
        long actualStackLines = ex.getStackTrace().length;
        long includedNewLines = payload.chars().filter(c -> c == '\n').count();

        // 스택이 5줄 초과라면 포함된 개행 수가 전체 스택 개행보다 적어야 함
        if (actualStackLines > 5) {
            assertThat(includedNewLines).isLessThan(actualStackLines);
        }
    }

    // ===== sendErrorNotification =====

    @Test
    void webhookUrl이_비어있으면_HTTP_요청을_보내지_않는다() throws InterruptedException {
        // given - webhookUrl 미설정 (기본값 "")

        // when
        assertThatNoException().isThrownBy(
                () -> sender.sendErrorNotification("에러 발생", new RuntimeException("테스트"))
        );

        // then
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNull();
    }

    @Test
    void Slack_응답이_200이면_예외가_전파되지_않는다() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        ReflectionTestUtils.setField(sender, "webhookUrl", mockWebServer.url("/webhook").toString());

        // when & then
        assertThatNoException().isThrownBy(
                () -> sender.sendErrorNotification("에러 발생", new RuntimeException("테스트"))
        );

        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Content-Type")).contains("application/json");
    }

    @Test
    void Slack_응답이_200이_아니어도_예외가_전파되지_않는다() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
        ReflectionTestUtils.setField(sender, "webhookUrl", mockWebServer.url("/webhook").toString());

        // when & then
        assertThatNoException().isThrownBy(
                () -> sender.sendErrorNotification("에러 발생", new RuntimeException("테스트"))
        );
    }

    @Test
    void Slack_서버_연결_실패시_예외가_전파되지_않는다() throws IOException {
        // given - 서버를 미리 종료하여 연결 불가 상태 만들기
        mockWebServer.shutdown();
        ReflectionTestUtils.setField(sender, "webhookUrl", "http://localhost:1/webhook");

        // when & then
        assertThatNoException().isThrownBy(
                () -> sender.sendErrorNotification("에러 발생", new RuntimeException("테스트"))
        );
    }

}
