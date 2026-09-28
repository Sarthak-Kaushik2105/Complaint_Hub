package com.complainthub.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class ResponseUtil {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(
                new JavaTimeModule()
        );
    }

    private ResponseUtil() {
    }

    public static void sendSuccess(
            HttpServletResponse response,
            int status,
            String message,
            Object data
    ) throws IOException {

        ApiResponse<Object> apiResponse =
                new ApiResponse<>(
                        true,
                        message,
                        data
                );

        send(
                response,
                status,
                apiResponse
        );
    }

    public static void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        ApiResponse<Object> apiResponse =
                new ApiResponse<>(
                        false,
                        message,
                        null
                );

        send(
                response,
                status,
                apiResponse
        );
    }

    private static void send(
            HttpServletResponse response,
            int status,
            ApiResponse<?> apiResponse
    ) throws IOException {

        response.setStatus(status);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                OBJECT_MAPPER.writeValueAsString(
                        apiResponse
                )
        );
    }
}