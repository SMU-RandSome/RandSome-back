package org.smu.randsome.randsomeback.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ServicePeriodInterceptorTest extends UnitTestSupport {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ServicePeriodInterceptor createInterceptor(
            LocalDateTime serviceOpen,
            LocalDateTime matchingOpen,
            LocalDateTime matchingClose
    ) {
        return new ServicePeriodInterceptor(
                serviceOpen.format(FORMATTER),
                matchingOpen.format(FORMATTER),
                matchingClose.format(FORMATTER)
        );
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    @Test
    void 서비스_오픈_전_일반_API_요청시_SERVICE_NOT_YET_OPEN_예외가_발생한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11)
        );

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(
                request("/v1/candidate-registrations"),
                new MockHttpServletResponse(),
                new Object()
        ))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.SERVICE_NOT_YET_OPEN);
    }

    @Test
    void 서비스_오픈_전_매칭_API_요청시에도_SERVICE_NOT_YET_OPEN_예외가_발생한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11)
        );

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(
                request("/v1/matchings"),
                new MockHttpServletResponse(),
                new Object()
        ))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.SERVICE_NOT_YET_OPEN);
    }

    @Test
    void 서비스_오픈_후_매칭_오픈_전_일반_API_요청은_통과한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11)
        );

        // when
        boolean result = interceptor.preHandle(
                request("/v1/candidate-registrations"),
                new MockHttpServletResponse(),
                new Object()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 서비스_오픈_후_매칭_오픈_전_매칭_API_요청시_MATCHING_NOT_YET_OPEN_예외가_발생한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11)
        );

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(
                request("/v1/matchings"),
                new MockHttpServletResponse(),
                new Object()
        ))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.MATCHING_NOT_YET_OPEN);
    }

    @Test
    void 매칭_오픈_전_매칭_상세_조회_API도_차단된다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11)
        );

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(
                request("/v1/matchings/applications/1"),
                new MockHttpServletResponse(),
                new Object()
        ))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.MATCHING_NOT_YET_OPEN);
    }

    @Test
    void 매칭_오픈_기간_중_매칭_API_요청이_통과한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );

        // when
        boolean result = interceptor.preHandle(
                request("/v1/matchings"),
                new MockHttpServletResponse(),
                new Object()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 매칭_마감_후_매칭_API_요청시_MATCHING_PERIOD_CLOSED_예외가_발생한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        // when & then
        assertThatThrownBy(() -> interceptor.preHandle(
                request("/v1/matchings"),
                new MockHttpServletResponse(),
                new Object()
        ))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.MATCHING_PERIOD_CLOSED);
    }

    @Test
    void 매칭_마감_후_일반_API_요청은_통과한다() {
        // given
        ServicePeriodInterceptor interceptor = createInterceptor(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        // when
        boolean result = interceptor.preHandle(
                request("/v1/candidate-registrations"),
                new MockHttpServletResponse(),
                new Object()
        );

        // then
        assertThat(result).isTrue();
    }

}
