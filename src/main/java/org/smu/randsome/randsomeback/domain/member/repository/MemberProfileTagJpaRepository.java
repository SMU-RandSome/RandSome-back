package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileTagJpaRepository extends JpaRepository<MemberProfileTag, Long> {

    Optional<MemberProfileTag> findByMemberId(Long memberId);

    List<MemberProfileTag> findAllByMemberIdIn(List<Long> memberIds);

}