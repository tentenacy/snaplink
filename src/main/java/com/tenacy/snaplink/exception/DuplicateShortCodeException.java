package com.tenacy.snaplink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateShortCodeException extends RuntimeException {
    public DuplicateShortCodeException(String shortCode) {
        super("Short code " + shortCode + " already exists");
    }
}

