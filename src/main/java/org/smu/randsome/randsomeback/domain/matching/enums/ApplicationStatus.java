package org.smu.randsome.randsomeback.domain.matching.enums;

public enum ApplicationStatus {

    PENDING,
    SUCCESS,
    PARTIAL_MATCH,
    FAILED,
    ;

    public boolean isCompleted() {
        return this == SUCCESS || this == PARTIAL_MATCH || this == FAILED;
    }

}