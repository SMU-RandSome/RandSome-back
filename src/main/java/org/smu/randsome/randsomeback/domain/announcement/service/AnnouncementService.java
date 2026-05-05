package org.smu.randsome.randsomeback.domain.announcement.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.implement.AnnouncementReader;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AnnouncementService {

    private final AnnouncementReader announcementReader;

    public List<AnnouncementItem> findAnnouncements() {
        return announcementReader.findAnnouncements();
    }

}