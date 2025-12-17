package com.sentinel.tenant_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Role is required")
    private String role; // TENANT_ADMIN o TENANT_USER

    /**
     * Optional list of project IDs the member will have access to.
     * If null or empty, member gets access to all projects in the tenant.
     */
    private List<UUID> projectIds;
}
