package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

    public MemberProfileTag find(Long memberId) {
        return memberProfileTagJpaRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Map<Long, MemberProfileTag> findAllByMemberIds(List<Long> memberIds) {
        return memberProfileTagJpaRepository.findAllByMemberIdIn(memberIds)
                .stream()
                .collect(Collectors.toMap(
                        tag -> tag.getMember().getId(),
                        Function.identity()
                ));
    }

}
