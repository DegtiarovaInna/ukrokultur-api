package de.ukrokultur.ukrokultur_api.common.exception;

import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " not found: " + id);
    }
}
