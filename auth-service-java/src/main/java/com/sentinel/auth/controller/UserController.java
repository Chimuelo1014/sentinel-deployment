package com.sentinel.auth.controller;

import com.sentinel.auth.dto.response.UserResponseDTO;
import com.sentinel.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Admin: List all users
     * GET /api/users
     */
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: Validate SUPER_ADMIN role
        log.info("Fetching ALL users (admin)");
        return ResponseEntity.ok(userService.getAllUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    /**
     * Admin: Delete user (Soft Delete)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable java.util.UUID id) {
        log.info("Deleting user: {} (admin)", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin: Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable java.util.UUID id,
            @RequestBody com.sentinel.auth.dto.request.RegisterRequest request) { // Reusing RegisterRequest for
                                                                                  // simplicity or create
                                                                                  // UpdateUserRequest
        log.info("Updating user: {} (admin)", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Internal/Admin: Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable java.util.UUID id) {
        log.info("Fetching user by ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
