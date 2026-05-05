package org.smu.randsome.randsomeback.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Department {

    // 글로벌지역학부
    KOREAN_LANGUAGE_CULTURE               ("한국언어문화전공"),
    JAPANESE_REGIONAL_STUDIES             ("일본어권지역학전공"),
    CHINESE_REGIONAL_STUDIES              ("중국어권지역학전공"),
    ENGLISH_REGIONAL_STUDIES              ("영어권지역학전공"),
    FRENCH_REGIONAL_STUDIES               ("프랑스어권지역학전공"),
    GERMAN_REGIONAL_STUDIES               ("독일어권지역학전공"),
    RUSSIAN_CENTRAL_ASIA_REGIONAL_STUDIES ("러시아·중앙아시아지역학전공"),

    // 디자인대학
    COMMUNICATION_DESIGN ("커뮤니케이션디자인전공"),
    FASHION_DESIGN       ("패션디자인전공"),
    TEXTILE_DESIGN       ("텍스타일디자인전공"),
    SPACE_DESIGN         ("스페이스디자인전공"),
    CERAMIC_DESIGN       ("세라믹디자인전공"),
    INDUSTRIAL_DESIGN    ("인더스트리얼디자인전공"),
    AR_VR_MEDIA_DESIGN   ("AR·VR미디어디자인전공"),

    //예술대학
    FILM_VIDEO              ("영화영상전공"),
    THEATER                 ("연극전공"),
    STAGE_ART               ("무대미술전공"),
    PHOTO_VIDEO_MEDIA       ("사진영상미디어전공"),
    DIGITAL_COMICS_VIDEO    ("디지털만화영상전공"),
    ARTS_CULTURE_MANAGEMENT ("문화예술경영전공"),
    AI_MEDIA_CONTENT        ("AI미디어콘텐츠전공"),

    // 융합기술대학
    GLOBAL_FINANCE_MANAGEMENT ("글로벌금융경영학부"),
    FOOD_ENGINEERING          ("식품공학과"),
    GREEN_SMART_CITY          ("그린스마트시티학과"),
    NURSING                   ("간호학과"),
    BIO_FOOD_TECH             ("바이오푸드테크학과"),

    // 스포츠융합학부
    SPORTS_CONVERGENCE("스포츠융합학부"),

    // 공과대학
    ELECTRONICS_ENGINEERING              ("전자공학과"),
    SOFTWARE                             ("소프트웨어학과"),
    SMART_INFO_COMMUNICATION_ENGINEERING ("스마트정보통신공학과"),
    INDUSTRIAL_MANAGEMENT_ENGINEERING    ("경영공학과"),
    GREEN_CHEMICAL_ENGINEERING           ("그린화학공학과"),
    CIVIL_SYSTEM_ENGINEERING             ("건설시스템공학과"),
    INFORMATION_SECURITY_ENGINEERING     ("정보보안공학과"),
    SYSTEM_SEMICONDUCTOR_ENGINEERING     ("시스템반도체공학과"),
    HUMAN_INTELLIGENT_ROBOT_ENGINEERING  ("휴먼지능로봇공학과"),
    INTELLIGENT_ROBOTICS                 ("지능형로봇학과"),
    AI_MOBILITY_ENGINEERING              ("AI모빌리티공학과"),
    SMART_IT_CONVERGENCE_ENGINEERING     ("스마트IT융합공학과"),

    // 자율전공
    SELF_DIRECTED_MAJOR ("자율전공"),
    ;

    private final String displayName;

    public boolean isSelfDirectedMajor() {
        return this == SELF_DIRECTED_MAJOR;
    }

}