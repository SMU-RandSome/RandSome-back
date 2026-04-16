package org.smu.randsome.randsomeback.admin.qr.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.qr.dto.request.QrVerifyRequest;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class QrAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_QR인증_후_티켓_발급에_성공하면_200을_반환한다() throws JsonProcessingException {
        // given
        var request = new QrVerifyRequest("valid.qr.token", TicketType.IDEAL);
        willDoNothing().given(qrAdminService).verifyQrAndIssueTicket(anyString(), any());

        // when & then
        assertThat(mvcTester.post()
                .uri("/v1/admin/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));

        then(qrAdminService).should().verifyQrAndIssueTicket(anyString(), any());
    }


    @TestAdmin
    @Test
    void QR토큰이_만료되었으면_401을_반환한다() throws JsonProcessingException {
        // given
        var request = new QrVerifyRequest("expired.token", TicketType.IDEAL);
        willThrow(new CoreException(ErrorType.QR_TOKEN_EXPIRED))
                .given(qrAdminService).verifyQrAndIssueTicket(anyString(), any());

        // when & then
        assertThat(mvcTester.post()
                .uri("/v1/admin/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"));
    }

    @TestAdmin
    @Test
    void 이미_사용된_QR토큰이면_409를_반환한다() throws JsonProcessingException {
        // given
        var request = new QrVerifyRequest("already-used.token", TicketType.RANDOM);
        willThrow(new CoreException(ErrorType.QR_TOKEN_ALREADY_USED))
                .given(qrAdminService).verifyQrAndIssueTicket(anyString(), any());

        // when & then
        assertThat(mvcTester.post()
                .uri("/v1/admin/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.CONFLICT.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"));
    }

    @TestAdmin
    @Test
    void 회원을_찾을_수_없으면_404를_반환한다() throws JsonProcessingException {
        // given
        var request = new QrVerifyRequest("valid.token", TicketType.IDEAL);
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(qrAdminService).verifyQrAndIssueTicket(anyString(), any());

        // when & then
        assertThat(mvcTester.post()
                .uri("/v1/admin/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"));
    }

    @TestAdmin
    @Test
    void QR토큰이_비어있으면_400을_반환한다() throws JsonProcessingException {
        // given
        var request = new QrVerifyRequest("", TicketType.IDEAL);

        // when & then
        assertThat(mvcTester.post()
                .uri("/v1/admin/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"));
    }

    @TestAdmin
    @Test
    void 티켓타입이_null이면_400을_반환한다() throws JsonProcessingException {
        // given
        String jsonRequest = """
                {
                    "qrToken": "valid.token"
                }
                """;

        // when & then
        assertThat(mvcTester.post()
                .uri("/v1/admin/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"));
    }

}
