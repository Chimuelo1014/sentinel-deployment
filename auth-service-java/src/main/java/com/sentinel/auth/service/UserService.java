package com.sentinel.auth.service;

import com.sentinel.auth.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    void deleteUser(java.util.UUID id);

    UserResponseDTO updateUser(java.util.UUID id, com.sentinel.auth.dto.request.RegisterRequest request);

    UserResponseDTO getUserById(java.util.UUID id);
}
