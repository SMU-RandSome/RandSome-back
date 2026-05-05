package org.smu.randsome.randsomeback.domain.matching.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.strategy.MatchingStrategy;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MatchingExecutor {

    private final MatchingResultJpaRepository matchingResultJpaRepository;
    private final Map<MatchingType, MatchingStrategy> strategyMap;
    private final ApplicationEventPublisher eventPublisher;

    public MatchingExecutor(
            MatchingResultJpaRepository matchingResultJpaRepository,
            List<MatchingStrategy> strategies,
            ApplicationEventPublisher eventPublisher
    ) {
        this.matchingResultJpaRepository = matchingResultJpaRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(MatchingStrategy::getSupportedType, Function.identity()));
        this.eventPublisher = eventPublisher;
    }

    /**
     * 매칭 신청에 대해 타입별 전략으로 매칭 결과를 생성하고, 신청을 완료 상태로 전이한다.
     * </br> 매칭 완료 후 피드에 기록할 수 있도록 이벤트를 발행한다.
     *
     * @param matchingApplication 매칭 신청 엔티티
     */
    public void execute(MatchingApplication matchingApplication) {
        MatchingStrategy strategy = resolveStrategy(matchingApplication.getMatchingType());
        List<MatchingResult> results = strategy.execute(matchingApplication);

        matchingResultJpaRepository.saveAll(results);
        matchingApplication.complete(LocalDateTime.now(), results.size());

        log.info("[MatchingExecutor] 매칭 완료 - matchingApplicationId: {}, matchingType: {}, applicationCount: {}, resultCount: {}",
                matchingApplication.getId(), matchingApplication.getMatchingType(),
                matchingApplication.getApplicationCount(), results.size());

        eventPublisher.publishEvent(new MatchingApplicationCompletedEvent(
                matchingApplication.getId(),
                matchingApplication.getMember().getNickname(),
                matchingApplication.getApplicationCount(),
                matchingApplication.getMatchedCount(),
                matchingApplication.getApplicationStatus()
        ));
    }

    private MatchingStrategy resolveStrategy(MatchingType matchingType) {
        MatchingStrategy strategy = strategyMap.get(matchingType);
        if (strategy == null) {
            log.error("[MatchingExecutor] 지원하지 않는 매칭 타입 - matchingType: {}", matchingType);
            throw new CoreException(ErrorType.UNSUPPORTED_MATCHING_TYPE);
        }
        return strategy;
    }

}
