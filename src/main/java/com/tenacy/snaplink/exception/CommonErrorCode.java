package com.tenacy.snaplink.exception;

import com.tenacy.snaplink.doc.ErrorExplanation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

@Getter
public enum CommonErrorCode implements BaseErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "SNAP-1000", "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SNAP-1001", "알 수 없는 에러가 발생했습니다."),
    URL_EXPIRED(HttpStatus.BAD_REQUEST.value(), "SNAP-2000", "기한이 만료된 단축 URL입니다."),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "SNAP-2001", "존재하지 않는 단축 URL입니다."),
    SHORT_CODE_DUPLICATED(HttpStatus.BAD_REQUEST.value(), "SNAP-2002", "이미 중복된 단축 URL이 존재합니다."),
    ;

    public static final String _URL_EXPIRED = "SNAP-2000";
    public static final String _URL_NOT_FOUND = "SNAP-2001";
    public static final String _SHORT_CODE_DUPLICATED = "SNAP-2002";

    private int status;
    private final String code;
    private final String reason;

    CommonErrorCode(final int status, final String code, final String reason) {
        this.status = status;
        this.reason = reason;
        this.code = code;
    }

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.builder().reason(this.reason).code(this.code).status(this.status).build();
    }

    @Override
    public String getErrorExplanation() throws NoSuchFieldException {
        Field field = this.getClass().getField(this.name());
        ErrorExplanation annotation = field.getAnnotation(ErrorExplanation.class);
        return Objects.nonNull(annotation) ? annotation.value() : this.getReason();
    }

    @Override
    public BaseErrorCode find(String value) throws NoSuchFieldException {
        return Arrays.stream(values()).filter((errorCode) -> errorCode.getCode().equals(value)).findAny().orElse(null);
    }
}