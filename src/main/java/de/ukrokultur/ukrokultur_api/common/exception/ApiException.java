package de.ukrokultur.ukrokultur_api.common.exception;

import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;

public class ApiException extends RuntimeException {
    private final int status;
    private final ErrorCode code;

    public ApiException(int status, ErrorCode code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() { return status; }
    public ErrorCode getCode() { return code; }
}
