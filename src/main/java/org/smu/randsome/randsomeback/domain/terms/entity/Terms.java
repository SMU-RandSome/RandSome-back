package org.smu.randsome.randsomeback.domain.terms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Terms extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private boolean required;

    public static Terms register(String title, String content, String version, boolean required) {
        Terms terms = new Terms();

        terms.title = title;
        terms.content = content;
        terms.version = version;
        terms.required = required;

        return terms;
    }

}