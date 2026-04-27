package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSearchCondition;
import org.smu.randsome.randsomeback.domain.member.entity.Member;

public interface MemberQueryDslRepository {

    List<Member> findAllNonDeletedNonAdmin(MemberSearchCondition condition, long offset, long limit);

    long countNonDeletedNonAdmin(MemberSearchCondition condition);

}
