package org.smu.randsome.randsomeback.global.support.error;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

    private final ErrorType errorType;
    private final Object data;

    public CoreException(ErrorType type) {
        super(type.getMessage());
        this.errorType = type;
        this.data = null;
    }

    public CoreException(ErrorType type, Object data) {
        super(type.getMessage());
        this.errorType = type;
        this.data = data;
    }

}