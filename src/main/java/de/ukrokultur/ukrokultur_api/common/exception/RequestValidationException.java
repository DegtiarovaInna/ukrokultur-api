package de.ukrokultur.ukrokultur_api.common.exception;

import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;

import java.util.Map;

public class RequestValidationException extends RuntimeException {

    private final int status;
    private final ErrorCode code;
    private final Map<String, String> fieldErrors;

    public RequestValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.status = 400;
        this.code = ErrorCode.VALIDATION_ERROR;
        this.fieldErrors = fieldErrors;
    }

    public int getStatus() {
        return status;
    }

    public ErrorCode getCode() {
        return code;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}