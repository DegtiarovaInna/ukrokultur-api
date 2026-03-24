package de.ukrokultur.ukrokultur_api.common.web;

import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.RequestValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MultipartJsonReader {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public MultipartJsonReader(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public <T> T read(String json, Class<T> type) {
        final T value;

        try {
            value = objectMapper.readValue(json, type);
        } catch (Exception ex) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Invalid JSON in multipart 'data'");
        }


        Set<ConstraintViolation<T>> violations = validator.validate(value);

        if (!violations.isEmpty()) {
            Map<String, String> fieldErrors = new LinkedHashMap<>();

            for (ConstraintViolation<T> violation : violations) {
                String field = violation.getPropertyPath() == null
                        ? null
                        : violation.getPropertyPath().toString();

                if (field == null || field.isBlank()) {
                    field = "request";
                }


                fieldErrors.putIfAbsent(field, violation.getMessage());
            }

            throw new RequestValidationException("Validation failed", fieldErrors);
        }

        return value;
    }
}