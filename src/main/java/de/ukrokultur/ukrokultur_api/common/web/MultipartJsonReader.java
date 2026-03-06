package de.ukrokultur.ukrokultur_api.common.web;

import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class MultipartJsonReader {

    private final ObjectMapper objectMapper;

    public MultipartJsonReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Invalid JSON in multipart 'data'");
        }
    }
}