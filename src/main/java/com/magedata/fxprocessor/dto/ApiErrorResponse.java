package com.magedata.fxprocessor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> validationErrors
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OffsetDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<String> validationErrors;

        public Builder timestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder validationErrors(List<String> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(timestamp, status, error, message, path, validationErrors);
        }
    }
}
