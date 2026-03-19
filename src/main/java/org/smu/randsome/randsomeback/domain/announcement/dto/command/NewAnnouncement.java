package org.smu.randsome.randsomeback.domain.announcement.dto.command;

import lombok.Builder;

@Builder
public record NewAnnouncement(
        String title,
        String content
) {

}