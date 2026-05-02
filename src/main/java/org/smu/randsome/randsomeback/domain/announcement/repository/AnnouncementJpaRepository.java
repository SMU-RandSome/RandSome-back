package org.smu.randsome.randsomeback.domain.announcement.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementJpaRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByStatusOrderByIdDesc(EntityStatus status);

    Optional<Announcement> findByIdAndStatus(Long id, EntityStatus status);
}