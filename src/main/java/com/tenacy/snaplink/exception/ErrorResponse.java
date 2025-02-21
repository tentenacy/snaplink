package com.tenacy.snaplink.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    private String code;
    private int status;
    private String message;
    private List<FieldError> errors;

    private ErrorResponse(final BaseErrorCode code) {
        ErrorReason errorReason = code.getErrorReason();
        this.code = errorReason.getCode();
        this.status = errorReason.getStatus();
        this.message = errorReason.getReason();
        this.errors = new ArrayList<>();
    }

    private ErrorResponse(final BaseErrorCode code, final List<FieldError> errors) {
        ErrorReason errorReason = code.getErrorReason();
        this.code = errorReason.getCode();
        this.status = errorReason.getStatus();
        this.message = errorReason.getReason();
        this.errors = errors;
    }

    public static ErrorResponse of(final BaseErrorCode code, final BindingResult bindingResult) {
        return new ErrorResponse(code, FieldError.of(bindingResult));
    }

    public static ErrorResponse of(final BaseErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        public FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        private static List<FieldError> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .collect(Collectors.toList());
        }
    }
}