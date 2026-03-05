package org.smu.randsome.randsomeback.domain.member.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    /**
 * Determines whether a member with the specified email address and status exists.
 *
 * @param email  the email address to search for (local-part@domain)
 * @param status the entity status to match
 * @return       {@code true} if a matching member exists, {@code false} otherwise
 */
boolean existsByEmail_AddressAndStatus(String email, EntityStatus status);
    /**
 * Finds a member by email address and entity status.
 *
 * @param email  the member's email address to search for
 * @param status the entity status to match
 * @return an Optional containing the matching Member if found, or Optional.empty() if not
 */
Optional<Member> findByEmail_AddressAndStatus(String email, EntityStatus status);

}