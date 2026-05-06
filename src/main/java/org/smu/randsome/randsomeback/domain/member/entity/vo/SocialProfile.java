package org.smu.randsome.randsomeback.domain.member.entity.vo;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record SocialProfile(
        @Column(nullable = false)
        String instagramId,

        @Column(length = 1000)
        String selfIntroduction,

        @Column(length = 1000)
        String idealDescription
) {

    public static SocialProfile create(String instagramId, String selfIntroduction, String idealDescription) {
        return new SocialProfile(requireNonNull(instagramId), selfIntroduction, idealDescription);
    }

}