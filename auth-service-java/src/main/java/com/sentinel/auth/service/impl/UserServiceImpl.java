package com.sentinel.auth.service.impl;

import com.sentinel.auth.dto.response.UserResponseDTO;
import com.sentinel.auth.entity.UserEntity;
import com.sentinel.auth.enums.UserStatus;
import com.sentinel.auth.repository.UserRepository;
import com.sentinel.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sentinel.auth.dto.request.RegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void deleteUser(java.util.UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(java.util.UUID id, RegisterRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        // Ideally handle password update separately or here if included

        UserEntity updated = userRepository.save(user);
        return mapToDTO(updated);
    }

    private UserResponseDTO mapToDTO(UserEntity user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .globalRole(user.getGlobalRole().name())
                .authProvider(user.getAuthProvider().name())
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(java.util.UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }
}
