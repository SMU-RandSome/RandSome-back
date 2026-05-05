package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class MemberProfileTagReader {

    private final MemberProfileTagJpaRepository memberProfileTagJpaRepository;
    private final MemberProfileTagCacheManager profileTagCacheManager;

    public ProfileTags find(Long memberId) {
        Optional<ProfileTags> cached = profileTagCacheManager.get(memberId);
        if (cached.isPresent()) {
            return cached.get();
        }

        MemberProfileTag tag = memberProfileTagJpaRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
        ProfileTags profileTags = ProfileTags.from(tag);
        profileTagCacheManager.put(memberId, profileTags);

        return profileTags;
    }

    public Map<Long, ProfileTags> findAllByMemberIds(List<Long> memberIds) {
        Map<Long, ProfileTags> result = HashMap.newHashMap(memberIds.size());
        List<Long> missedIds = profileTagCacheManager.getAll(memberIds, result);

        if (!missedIds.isEmpty()) {
            loadFromDb(missedIds, result);
        }

        return result;
    }

    private void loadFromDb(List<Long> missedIds, Map<Long, ProfileTags> result) {
        List<MemberProfileTag> fromDb = memberProfileTagJpaRepository.findAllByMemberIdIn(missedIds);

        Map<Long, ProfileTags> loaded = HashMap.newHashMap(fromDb.size());
        for (MemberProfileTag tag : fromDb) {
            Long memberId = tag.getMember().getId();
            ProfileTags profileTags = ProfileTags.from(tag);
            result.put(memberId, profileTags);
            loaded.put(memberId, profileTags);
        }

        profileTagCacheManager.putAll(loaded);
    }

}
