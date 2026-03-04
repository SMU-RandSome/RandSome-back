package org.smu.randsome.randsomeback.global.config;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;

/**
 * 타임존 설정
 *
 * <p>애플리케이션 전체의 타임존을 Asia/Seoul로 설정합니다.
 * 이를 통해 로그, 데이터베이스 시간 등이 한국 시간(KST)으로 일관되게 처리됩니다.
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

}