package de.ukrokultur.ukrokultur_api.common.error;

import jakarta.validation.ConstraintViolation;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

@Component
public class ValidationMessageResolver {

    public String resolve(FieldError error) {
        String field = error.getField();

        if (hasCode(error, "NotNull") || hasCode(error, "NotBlank")) {
            return field + " is required";
        }

        if (hasCode(error, "NotEmpty")) {
            return field + " must not be empty";
        }

        if (hasCode(error, "Email")) {
            return field + " must be a valid email address";
        }

        if (hasCode(error, "Size")) {
            return field + " has invalid length";
        }

        if (hasCode(error, "Pattern")) {
            return field + " has invalid format";
        }

        if (hasCode(error, "Min")) {
            return field + " is too small";
        }

        if (hasCode(error, "Max")) {
            return field + " is too large";
        }

        if (hasCode(error, "Positive")) {
            return field + " must be positive";
        }

        if (hasCode(error, "PositiveOrZero")) {
            return field + " must be positive or zero";
        }

        if (hasCode(error, "Negative")) {
            return field + " must be negative";
        }

        if (hasCode(error, "NegativeOrZero")) {
            return field + " must be negative or zero";
        }

        if (hasCode(error, "AssertTrue")) {
            return field + " must be accepted";
        }

        if (hasCode(error, "AssertFalse")) {
            return field + " must be false";
        }

        return field + " is invalid";
    }

    public String resolve(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath() == null
                ? "request"
                : violation.getPropertyPath().toString();

        if (field == null || field.isBlank()) {
            field = "request";
        }

        String constraint = violation.getConstraintDescriptor()
                .getAnnotation()
                .annotationType()
                .getSimpleName();

        return switch (constraint) {
            case "NotNull", "NotBlank" -> field + " is required";
            case "NotEmpty" -> field + " must not be empty";
            case "Email" -> field + " must be a valid email address";
            case "Size" -> field + " has invalid length";
            case "Pattern" -> field + " has invalid format";
            case "Min" -> field + " is too small";
            case "Max" -> field + " is too large";
            case "Positive" -> field + " must be positive";
            case "PositiveOrZero" -> field + " must be positive or zero";
            case "Negative" -> field + " must be negative";
            case "NegativeOrZero" -> field + " must be negative or zero";
            case "AssertTrue" -> field + " must be accepted";
            case "AssertFalse" -> field + " must be false";
            default -> field + " is invalid";
        };
    }

    private boolean hasCode(FieldError error, String code) {
        if (error.getCodes() == null) {
            return false;
        }

        for (String c : error.getCodes()) {
            if (c != null && c.startsWith(code)) {
                return true;
            }
        }

        return false;
    }
}