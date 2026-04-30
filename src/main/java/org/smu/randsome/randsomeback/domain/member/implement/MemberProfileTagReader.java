package org.smu.randsome.randsomeback.domain.member.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class MemberProfileTagReader {

    private final MemberProfileTagJpaRepository memberProfileTagJpaRepository;
    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public MemberProfileTag find(Long memberId) {
        Optional<MemberProfileTag> cached = getFromCache(memberId);
        if (cached.isPresent()) {
            return cached.get();
        }

        MemberProfileTag tag = memberProfileTagJpaRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
        putToCache(memberId, tag);

        return tag;
    }

    public Map<Long, MemberProfileTag> findAllByMemberIds(List<Long> memberIds) {
        Map<Long, MemberProfileTag> result = HashMap.newHashMap(memberIds.size());
        List<Long> missedIds = getFromCacheAll(memberIds, result);

        if (!missedIds.isEmpty()) {
            loadFromDb(missedIds, result);
        }

        return result;
    }

    // ── 캐시 조회 ──

    private Optional<MemberProfileTag> getFromCache(Long memberId) {
        try {
            String json = redisRepository.get(CacheKeys.memberProfileTag(memberId));
            if (json != null) {
                return Optional.of(deserialize(json));
            }
        } catch (Exception e) {
            log.warn("[MemberProfileTagReader] 캐시 조회 실패, DB fallback. memberId={}", memberId, e);
        }
        return Optional.empty();
    }

    private List<Long> getFromCacheAll(List<Long> memberIds, Map<Long, MemberProfileTag> result) {
        try {
            List<String> keys = memberIds.stream()
                    .map(CacheKeys::memberProfileTag)
                    .toList();
            List<String> cached = redisRepository.mget(keys);

            List<Long> missedIds = new ArrayList<>();
            for (int i = 0; i < memberIds.size(); i++) {
                String value = cached.get(i);
                if (value != null) {
                    result.put(memberIds.get(i), deserialize(value));
                } else {
                    missedIds.add(memberIds.get(i));
                }
            }
            return missedIds;
        } catch (Exception e) {
            log.warn("[MemberProfileTagReader] 캐시 일괄 조회 실패, 전체 DB fallback", e);
            return new ArrayList<>(memberIds);
        }
    }

    // ── DB 조회 + 캐시 적재 ──

    private void loadFromDb(List<Long> missedIds, Map<Long, MemberProfileTag> result) {
        List<MemberProfileTag> fromDb = memberProfileTagJpaRepository.findAllByMemberIdIn(missedIds);
        for (MemberProfileTag tag : fromDb) {
            Long memberId = tag.getMember().getId();
            putToCache(memberId, tag);
            result.put(memberId, tag);
        }
    }

    // ── 직렬화 / 역직렬화 ──

    private void putToCache(Long memberId, MemberProfileTag tag) {
        try {
            String json = objectMapper.writeValueAsString(new CachedProfileTag(
                    tag.getPersonalityTag(),
                    tag.getFaceTypeTag(),
                    tag.getDatingStyleTag())
            );
            redisRepository.put(CacheKeys.memberProfileTag(memberId), json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("[MemberProfileTagReader] 캐시 저장 실패. memberId={}", memberId, e);
        }
    }

    private MemberProfileTag deserialize(String json) throws JsonProcessingException {
        CachedProfileTag cached = objectMapper.readValue(json, CachedProfileTag.class);
        return MemberProfileTag.forCache(
                cached.personalityTag(),
                cached.faceTypeTag(),
                cached.datingStyleTag()
        );
    }

    private record CachedProfileTag(
            PersonalityTag personalityTag,
            FaceTypeTag faceTypeTag,
            DatingStyleTag datingStyleTag
    ) {

    }

}
