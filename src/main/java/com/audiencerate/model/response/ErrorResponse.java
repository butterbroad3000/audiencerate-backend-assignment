package com.audiencerate.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int code, String message, Map<String, String> details) {
    public ErrorResponse(int code, String message) {
        this(code, message, null);
    }
}
