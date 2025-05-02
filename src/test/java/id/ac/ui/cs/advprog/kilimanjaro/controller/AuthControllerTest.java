package id.ac.ui.cs.advprog.kilimanjaro.controller;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.GenericResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterCustomerRequest;
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

    private RegisterCustomerRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private GenericResponse<LoginResponse> successLoginResponse;
    private GenericResponse<Void> successRegistrationResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterCustomerRequest();
        validRegisterRequest.setFullName("John Doe");
        validRegisterRequest.setEmail("john@example.com");
        validRegisterRequest.setPhoneNumber("+628123456789");
        validRegisterRequest.setPassword("securePassword123");
        validRegisterRequest.setAddress("123 Main St");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("securePassword123");

        successLoginResponse = new GenericResponse<>(
                true,
                "Login successful",
                new LoginResponse("jwt.token.here", "Bearer")
        );

        successRegistrationResponse = new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Test
    void registerCustomer_WithValidRequest_ReturnsCreated() throws UserAlreadyExistsException {
        when(authService.registerCustomer(validRegisterRequest)).thenReturn(successRegistrationResponse);

        ResponseEntity<?> response = authController.registerCustomer(validRegisterRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(successRegistrationResponse, response.getBody());
        verify(authService, times(1)).registerCustomer(validRegisterRequest);
    }

    @Test
    void registerCustomer_WhenUserExists_ReturnsBadRequest() throws UserAlreadyExistsException {
        when(authService.registerCustomer(validRegisterRequest))
                .thenThrow(new UserAlreadyExistsException("Email already in use"));

        ResponseEntity<?> response = authController.registerCustomer(validRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());
        verify(authService, times(1)).registerCustomer(validRegisterRequest);
    }

    @Test
    void loginUser_WithValidCredentials_ReturnsOk() throws InvalidCredentialsException {
        when(authService.login(validLoginRequest)).thenReturn(successLoginResponse);

        ResponseEntity<?> response = authController.loginUser(validLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successLoginResponse, response.getBody());
        verify(authService, times(1)).login(validLoginRequest);
    }

    @Test
    void loginUser_WithInvalidCredentials_ReturnsUnauthorized() throws InvalidCredentialsException {
        when(authService.login(validLoginRequest))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        ResponseEntity<?> response = authController.loginUser(validLoginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(authService, times(1)).login(validLoginRequest);
    }

    @Test
    void logoutUser_WithValidToken_ReturnsNoContent() {
        String validToken = "Bearer jwt.token.here";

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