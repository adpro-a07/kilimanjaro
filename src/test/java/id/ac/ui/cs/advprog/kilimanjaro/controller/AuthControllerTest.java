package id.ac.ui.cs.advprog.kilimanjaro.controller;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.*;
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
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterCustomerRequest validCustomerRegisterRequest;
    private LoginRequest validLoginRequest;
    private GenericResponse<LoginResponse> successLoginResponse;
    private GenericResponse<Void> successRegistrationResponse;

    @BeforeEach
    void setUp() {
        // Setup valid customer register request
        validCustomerRegisterRequest = new RegisterCustomerRequest();
        validCustomerRegisterRequest.setFullName("John Doe");
        validCustomerRegisterRequest.setEmail("john@example.com");
        validCustomerRegisterRequest.setPhoneNumber("08123456789");
        validCustomerRegisterRequest.setPassword1("Password123!"); // Changed to match implementation
        validCustomerRegisterRequest.setPassword2("Password123!"); // Added confirmation password
        validCustomerRegisterRequest.setAddress("123 Main St");

        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("Password123!");

        // Setup success responses
        LoginResponse loginResponseData = new LoginResponse(
                "jwt-access-token",
                "jwt-refresh-token",
                "john@example.com"
        );

        successLoginResponse = new GenericResponse<>(
                true,
                "Login successful",
                loginResponseData
        );

        successRegistrationResponse = new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Test
    void registerCustomer_WithValidRequest_ReturnsCreated() {
        when(authService.registerCustomer(validCustomerRegisterRequest)).thenReturn(successRegistrationResponse);

        ResponseEntity<?> response = authController.registerCustomer(validCustomerRegisterRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(successRegistrationResponse, response.getBody());
        verify(authService, times(1)).registerCustomer(validCustomerRegisterRequest);
    }

    @Test
    void registerCustomer_WhenUserExists_ReturnsBadRequest() {
        when(authService.registerCustomer(validCustomerRegisterRequest))
                .thenThrow(new UserAlreadyExistsException("Email already in use"));

        ResponseEntity<?> response = authController.registerCustomer(validCustomerRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());
        verify(authService, times(1)).registerCustomer(validCustomerRegisterRequest);
    }

    @Test
    void registerCustomer_WithPasswordMismatch_ReturnsBadRequest() {
        validCustomerRegisterRequest.setPassword2("DifferentPassword123!");
        when(authService.registerCustomer(validCustomerRegisterRequest))
                .thenThrow(new IllegalArgumentException("Passwords do not match"));

        ResponseEntity<?> response = authController.registerCustomer(validCustomerRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Passwords do not match", response.getBody());
        verify(authService, times(1)).registerCustomer(validCustomerRegisterRequest);
    }

    @Test
    void loginUser_WithValidCredentials_ReturnsOk() {
        when(authService.login(validLoginRequest)).thenReturn(successLoginResponse);

        ResponseEntity<?> response = authController.loginUser(validLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successLoginResponse, response.getBody());
        verify(authService, times(1)).login(validLoginRequest);
    }

    @Test
    void loginUser_WithInvalidCredentials_ReturnsUnauthorized() {
        when(authService.login(validLoginRequest))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        ResponseEntity<?> response = authController.loginUser(validLoginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(authService, times(1)).login(validLoginRequest);
    }

    @Test
    void logoutUser_WithValidToken_ReturnsNoContent() {
        String validToken = "jwt-access-token";

        ResponseEntity<?> response = authController.logoutUser(validToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService, times(1)).logout(validToken);
    }

    @Test
    void logoutUser_WithEmptyToken_StillCallsService() {
        String emptyToken = "";

        ResponseEntity<?> response = authController.logoutUser(emptyToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authService, times(1)).logout(emptyToken);
    }
}