package de.ukrokultur.ukrokultur_api.common.web;


import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class MultipartJsonReader {

    private final JsonMapper jsonMapper;

    public MultipartJsonReader(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public <T> T read(String json, Class<T> type) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (Exception ex) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Invalid JSON in multipart 'data'");
        }
    }
}