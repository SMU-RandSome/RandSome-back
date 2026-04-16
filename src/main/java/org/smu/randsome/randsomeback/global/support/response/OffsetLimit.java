package org.smu.randsome.randsomeback.global.support.response;

/**
 * 페이지네이션을 위한 Offset과 Limit을 나타내는 클래스입니다.
 * </br> page는 1부터 시작하며, size는 1 이상 30 이하로 제한됩니다.
 * */
public record OffsetLimit(
        int page,
        int size
) {
    public OffsetLimit {
        if (page < 1) page = 1;
        if (size < 1) size = 1;
        if (size > 30) size = 30;
    }

    public long offset() {
        return (long) (page - 1) * size;
    }

    public long limit() {
        return size;
    }

}