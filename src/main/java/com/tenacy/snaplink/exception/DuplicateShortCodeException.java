package com.tenacy.snaplink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateShortCodeException extends SnapLinkException {
    public static final SnapLinkException EXCEPTION = new DuplicateShortCodeException();

    public DuplicateShortCodeException() {
        super(CommonErrorCode.SHORT_CODE_DUPLICATED);
    }
}

