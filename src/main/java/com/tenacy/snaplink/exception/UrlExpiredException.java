package com.tenacy.snaplink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String shortCode) {
        super("URL with short code " + shortCode + " is expired");
    }
}