package org.smu.randsome.randsomeback.domain.qr.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class QrControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void QR_발급에_성공하면_200과_PNG_이미지를_반환한다() {
        // given
        byte[] pngImage = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG 시그니처
        given(qrService.generateMemberQr(any())).willReturn(pngImage);

        // when & then
        assertThat(mvcTester.get().uri("/v1/qr"))
                .apply(print())
                .hasStatusOk()
                .hasContentType(MediaType.IMAGE_PNG);
    }

    @Test
    void 인증되지_않은_사용자는_QR_발급_시_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/qr"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void QR_이미지_생성_실패_시_500을_반환한다() {
        // given
        given(qrService.generateMemberQr(any()))
                .willThrow(new CoreException(ErrorType.QR_GENERATION_FAILED));

        // when & then
        assertThat(mvcTester.get().uri("/v1/qr"))
                .apply(print())
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}