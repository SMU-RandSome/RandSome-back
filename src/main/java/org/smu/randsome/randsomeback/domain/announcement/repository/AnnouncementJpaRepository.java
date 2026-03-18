package org.smu.randsome.randsomeback.domain.announcement.repository;

import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementJpaRepository extends JpaRepository<Announcement, Long> {

}