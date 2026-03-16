package org.smu.randsome.randsomeback.domain.feed;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MatchingFeedEvent extends BaseEntity {

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    private String nickname;

    private Integer requestCount; // 매칭 이벤트의 경우, 매칭된 상대방의 수를 나타냅니다.

    public static MatchingFeedEvent recordMatchRequest(String nickname, Integer requestCount) {
        return MatchingFeedEvent.builder()
                .eventType(EventType.MATCH_REQUESTED)
                .nickname(requireNonNull(nickname))
                .requestCount(requireNonNull(requestCount))
                .build();
    }

    public static MatchingFeedEvent recordCandidateRegister(String nickname) {
        return MatchingFeedEvent.builder()
                .eventType(EventType.CANDIDATE_REGISTERED)
                .nickname(requireNonNull(nickname))
                .build();
    }

}