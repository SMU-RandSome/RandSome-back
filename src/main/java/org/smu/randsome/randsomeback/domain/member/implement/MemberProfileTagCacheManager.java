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
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberProfileTagCacheManager {

    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public Optional<ProfileTags> get(Long memberId) {
        try {
            String json = redisRepository.get(CacheKeys.memberProfileTag(memberId));
            if (json != null) {
                return Optional.of(deserialize(json));
            }
        } catch (Exception e) {
            log.warn("[MemberProfileTagCacheManager] 캐시 조회 실패. memberId={}", memberId, e);
        }
        return Optional.empty();
    }

    public List<Long> getAll(List<Long> memberIds, Map<Long, ProfileTags> result) {
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
            log.warn("[MemberProfileTagCacheManager] 캐시 일괄 조회 실패, 전체 DB fallback", e);
            return new ArrayList<>(memberIds);
        }
    }

    public void put(Long memberId, ProfileTags profileTags) {
        try {
            redisRepository.put(CacheKeys.memberProfileTag(memberId), serialize(profileTags), CACHE_TTL);
        } catch (Exception e) {
            log.warn("[MemberProfileTagCacheManager] 캐시 저장 실패. memberId={}", memberId, e);
        }
    }

    public void putAll(Map<Long, ProfileTags> entries) {
        if (entries.isEmpty()) {
            return;
        }
        try {
            Map<String, String> cacheEntries = HashMap.newHashMap(entries.size());
            entries.forEach((memberId, profileTags) -> {
                try {
                    cacheEntries.put(CacheKeys.memberProfileTag(memberId), serialize(profileTags));
                } catch (JsonProcessingException e) {
                    log.warn("[MemberProfileTagCacheManager] 직렬화 실패. memberId={}", memberId, e);
                }
            });
            redisRepository.pipelinePut(cacheEntries, CACHE_TTL);
        } catch (Exception e) {
            log.warn("[MemberProfileTagCacheManager] 벌크 캐시 저장 실패", e);
        }
    }

    private String serialize(ProfileTags profileTags) throws JsonProcessingException {
        return objectMapper.writeValueAsString(profileTags);
    }

    private ProfileTags deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ProfileTags.class);
    }

}
