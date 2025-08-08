package com.tenacy.snaplink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UrlExpiredException extends SnapLinkException {
    public static final SnapLinkException EXCEPTION = new UrlExpiredException();

    public UrlExpiredException() {
        super(CommonErrorCode.URL_EXPIRED);
    }
}