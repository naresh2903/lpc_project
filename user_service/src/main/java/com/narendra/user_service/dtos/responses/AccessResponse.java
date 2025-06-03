package com.narendra.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessResponse {
    private String accessToken;
    private String refreshToken;
}
