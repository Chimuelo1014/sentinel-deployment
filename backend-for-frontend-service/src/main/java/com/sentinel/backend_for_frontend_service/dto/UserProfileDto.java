package com.sentinel.backend_for_frontend_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String avatar;
    private String role;
    private String createdAt;
}
