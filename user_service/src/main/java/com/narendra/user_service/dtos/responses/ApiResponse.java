package com.narendra.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // You can also add convenience constructors
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
