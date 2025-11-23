package com.splitBill.splitBill.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private String status;
    private String message;
    private T data;
    private Object errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("success")
                .message(message)
                .data(data)
                .build();
    }
    

    public static ApiResponse<?> success(String message) {
        return ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status("success")
                .message(message)
                .build();
    }

    public static ApiResponse<?> error(String message) {
        return ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status("error")
                .message(message)
                .build();
    }

    public static ApiResponse<?> error(String message, Object errors) {
        return ApiResponse.builder()
                .timestamp(LocalDateTime.now()) 
                .status("error")
                .message(message)
                .errors(errors)
                .build();
    }
}