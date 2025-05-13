package id.ac.ui.cs.advprog.kilimanjaro.controller;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.GenericResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterTechnicianRequest;
import id.ac.ui.cs.advprog.kilimanjaro.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminController adminController;

    private RegisterTechnicianRequest validRequest;
    private GenericResponse<Void> successResponse;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new RegisterTechnicianRequest();
        validRequest.setFullName("John Doe");
        validRequest.setEmail("john@example.com");
        validRequest.setPhoneNumber("+628123456789");
        validRequest.setPassword("securePassword123");
        validRequest.setAddress("123 Main St");
        // Add any other required fields for RegisterTechnicianRequest

        successResponse = new GenericResponse<>(
                true,
                "Technician registered successfully",
                null
        );
    }

    @Test
    void registerTechnician_WithValidRequest_ReturnsCreated() throws UserAlreadyExistsException {
        // Arrange
        when(authService.registerTechnician(validRequest)).thenReturn(successResponse);

        // Act
        ResponseEntity<?> response = adminController.registerTechnician(validRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(successResponse, response.getBody());
        verify(authService, times(1)).registerTechnician(validRequest);
    }

    @Test
    void registerTechnician_WhenUserExists_ReturnsBadRequest() throws UserAlreadyExistsException {
        // Arrange
        String errorMessage = "User with email john.doe@example.com already exists";
        when(authService.registerTechnician(validRequest))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        // Act
        ResponseEntity<?> response = adminController.registerTechnician(validRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(authService, times(1)).registerTechnician(validRequest);
    }

    @Test
    void registerTechnician_WithUnexpectedException_ThrowsException() {
        // Arrange
        String errorMessage = "Some unexpected error";
        RuntimeException exception = new RuntimeException(errorMessage);
        when(authService.registerTechnician(validRequest)).thenThrow(exception);

        // Act & Assert
        Exception thrown = assertThrows(RuntimeException.class, () ->
            adminController.registerTechnician(validRequest)
        );

        assertEquals(errorMessage, thrown.getMessage());
        verify(authService, times(1)).registerTechnician(validRequest);
    }
}