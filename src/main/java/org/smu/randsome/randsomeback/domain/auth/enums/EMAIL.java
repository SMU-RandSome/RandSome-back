package org.smu.randsome.randsomeback.domain.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EMAIL {

    EMAIL_SUBJECT      ("[Randsome] 이메일 인증 코드"),
    EMAIL_BODY_TEMPLATE("""
            안녕하세요, Randsome입니다.
            
            이메일 인증 코드: %s
            
            인증 코드는 5분간 유효합니다.
            본인이 요청하지 않은 경우 이 메일을 무시하세요.
            """);

    private final String value;

}