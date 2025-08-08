package com.tenacy.snaplink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UrlNotFoundException extends SnapLinkException {
    public static final SnapLinkException EXCEPTION = new UrlNotFoundException();

    public UrlNotFoundException() {
        super(CommonErrorCode.URL_NOT_FOUND);
    }
}