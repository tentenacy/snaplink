package com.tenacy.snaplink.exception;

import lombok.Getter;

@Getter
public class SnapLinkException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public SnapLinkException(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
