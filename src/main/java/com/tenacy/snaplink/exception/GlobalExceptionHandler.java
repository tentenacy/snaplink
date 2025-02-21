package com.tenacy.snaplink.exception;

import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final Counter errorCounter;

    /**
     * @Validated로 검증 시 binding 못하는 경우
     * CustomCollectionValidator로 검증 시 binding 못하는 경우
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(final BindException e) {
        errorCounter.increment();

        return new ResponseEntity<>(ErrorResponse.of(CommonErrorCode.INVALID_INPUT_VALUE, e.getBindingResult()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * 타입이 일치하지 않아 binding 못하는 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException() {
        errorCounter.increment();

        return new ResponseEntity<>(ErrorResponse.of(CommonErrorCode.INVALID_INPUT_VALUE), HttpStatus.BAD_REQUEST);
    }

    /**
     * SnapLinkException 하위 클래스
     */
    @ExceptionHandler(SnapLinkException.class)
    public ResponseEntity<ErrorResponse> handleSnapLinkException(final SnapLinkException e) {
        errorCounter.increment();

        BaseErrorCode code = e.getErrorCode();
        return new ResponseEntity<>(ErrorResponse.of(code), HttpStatus.valueOf(code.getErrorReason().getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        errorCounter.increment();

        return new ResponseEntity<>(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}