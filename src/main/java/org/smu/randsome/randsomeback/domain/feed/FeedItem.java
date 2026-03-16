package org.smu.randsome.randsomeback.domain.feed;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "매칭 피드 아이템")
public record FeedItem(
        @Schema(description = "피드 이벤트 ID", example = "123")
        Long id,

        @Schema(description = "이벤트 타입", example = "MATCHING_CREATED")
        EventType eventType,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "요청 수(매칭 후보 등록 이벤트일 경우 NULL)", example = "5")
        Integer requestCount,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt
) {

    public static FeedItem from(MatchingFeedEvent event) {
        return new FeedItem(
                event.getId(),
                event.getEventType(),
                event.getNickname(),
                event.getRequestCount(),
                event.getCreatedAt()
        );
    }

}
