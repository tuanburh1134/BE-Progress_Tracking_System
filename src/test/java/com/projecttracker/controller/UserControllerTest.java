package com.projecttracker.controller;

import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.entity.User;
import com.projecttracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @Test
    void listUsersShouldReturnPagedUserResponseWithCreatedAndUpdatedAt() {
        User user = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .passwordHash("hash")
                .fullName("System Admin")
                .role(User.UserRole.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 11, 0))
                .build();

        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

        ResponseEntity<ApiResponse<Page<com.projecttracker.dto.response.UserAdminResponse>>> response =
                userController.listUsers(0, 20);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());
        assertEquals("admin", response.getBody().getData().getContent().get(0).getUsername());
        assertEquals("ADMIN", response.getBody().getData().getContent().get(0).getRole());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), response.getBody().getData().getContent().get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 2, 11, 0), response.getBody().getData().getContent().get(0).getUpdatedAt());
    }
}
