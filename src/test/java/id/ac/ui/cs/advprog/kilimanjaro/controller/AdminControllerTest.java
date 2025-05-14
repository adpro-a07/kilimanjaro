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

    private RegisterTechnicianRequest validTechnicianRegisterRequest;
    private GenericResponse<Void> successRegistrationResponse;

    @BeforeEach
    void setUp() {
        // Setup valid technician register request
        validTechnicianRegisterRequest = new RegisterTechnicianRequest();
        validTechnicianRegisterRequest.setFullName("Tech Smith");
        validTechnicianRegisterRequest.setEmail("tech@example.com");
        validTechnicianRegisterRequest.setPhoneNumber("08765432100");
        validTechnicianRegisterRequest.setPassword1("Password123!");
        validTechnicianRegisterRequest.setPassword2("Password123!");
        validTechnicianRegisterRequest.setAddress("456 Tech Ave");
        validTechnicianRegisterRequest.setExperience("5 years in electronics repair");

        successRegistrationResponse = new GenericResponse<>(
                true,
                "Registration successful",
                null
        );

    }

    @Test
    void registerTechnician_WithValidRequest_ReturnsCreated() {
        when(authService.registerTechnician(validTechnicianRegisterRequest)).thenReturn(successRegistrationResponse);

        ResponseEntity<?> response = adminController.registerTechnician(validTechnicianRegisterRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(successRegistrationResponse, response.getBody());
        verify(authService, times(1)).registerTechnician(validTechnicianRegisterRequest);
    }

    @Test
    void registerTechnician_WhenUserExists_ReturnsBadRequest() {
        when(authService.registerTechnician(validTechnicianRegisterRequest))
                .thenThrow(new UserAlreadyExistsException("Email already in use"));

        ResponseEntity<?> response = adminController.registerTechnician(validTechnicianRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());
        verify(authService, times(1)).registerTechnician(validTechnicianRegisterRequest);
    }

    @Test
    void registerTechnician_WithPasswordMismatch_ReturnsBadRequest() {
        validTechnicianRegisterRequest.setPassword2("DifferentPassword123!");
        when(authService.registerTechnician(validTechnicianRegisterRequest))
                .thenThrow(new IllegalArgumentException("Passwords do not match"));

        ResponseEntity<?> response = adminController.registerTechnician(validTechnicianRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Passwords do not match", response.getBody());
        verify(authService, times(1)).registerTechnician(validTechnicianRegisterRequest);
    }

    @Test
    void registerTechnician_WithMissingExperience_ReturnsBadRequest() {
        validTechnicianRegisterRequest.setExperience(null);
        when(authService.registerTechnician(validTechnicianRegisterRequest))
                .thenThrow(new IllegalArgumentException("Experience is required"));

        ResponseEntity<?> response = adminController.registerTechnician(validTechnicianRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Experience is required", response.getBody());
        verify(authService, times(1)).registerTechnician(validTechnicianRegisterRequest);
    }
}