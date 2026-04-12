package org.smu.randsome.randsomeback.global.constant;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 서비스 기간을 정의하는 클래스입니다. <br>
 * OPEN_DATE: 서비스 시작 날짜 (2026년 5월 6일) <br>
 * CLOSE_DATE: 서비스 종료 날짜 (2026년 5월 28일)
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServicePeriod {

    public static final LocalDate OPEN_DATE = LocalDate.of(2026, 5, 6);
    public static final LocalDate CLOSE_DATE = LocalDate.of(2026, 5, 28);
    public static final int TOTAL_DAYS = (int) java.time.temporal.ChronoUnit.DAYS.between(OPEN_DATE, CLOSE_DATE) + 1;

}