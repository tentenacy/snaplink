package com.tenacy.snaplink.exception;

public interface BaseErrorCode {

    ErrorReason getErrorReason();
    String getErrorExplanation() throws NoSuchFieldException;
    BaseErrorCode find(String code) throws NoSuchFieldException;
}
