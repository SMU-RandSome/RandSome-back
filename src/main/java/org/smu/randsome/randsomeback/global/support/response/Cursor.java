package org.smu.randsome.randsomeback.global.support.response;

import lombok.Builder;

@Builder
public record Cursor(
        Long lastCursorId,
        int limit
) {
    public static final int MIN_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 30;

    public Cursor {
        if (limit < MIN_PAGE_SIZE) {
            limit = MIN_PAGE_SIZE;
        } else if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
    }

    public static Cursor of(Long lastCursorId, int limit) {
        return new Cursor(lastCursorId, limit);
    }

}