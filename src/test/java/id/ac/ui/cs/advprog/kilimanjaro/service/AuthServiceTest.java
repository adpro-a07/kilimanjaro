package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.AuthResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterCustomerRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterTechnicianRequest;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterCustomerRequest registerCustomerRequest;
    private RegisterTechnicianRequest registerTechnicianRequest;
    private LoginRequest loginRequest;
    private Customer customer;
    private Technician technician;

    @BeforeEach
    void setUp() {
        registerCustomerRequest = new RegisterCustomerRequest();
        registerCustomerRequest.setFullName("Test Customer");
        registerCustomerRequest.setEmail("customer@example.com");
        registerCustomerRequest.setPassword("password123");
        registerCustomerRequest.setPhoneNumber("123456789");
        registerCustomerRequest.setAddress("123 Customer Street");

        registerTechnicianRequest = new RegisterTechnicianRequest();
        registerTechnicianRequest.setFullName("Test Technician");
        registerTechnicianRequest.setEmail("technician@example.com");
        registerTechnicianRequest.setPassword("password123");
        registerTechnicianRequest.setPhoneNumber("987654321");
        registerTechnicianRequest.setAddress("456 Technician Avenue");
        registerTechnicianRequest.setExperience("5 years");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("customer@example.com");
        loginRequest.setPassword("password123");

        customer = new Customer.CustomerBuilder()
                .fullName("Test Customer")
                .email("customer@example.com")
                .phoneNumber("08123456789")
                .password("encoded_password")
                .address("Jl. Merdeka No. 1")
                .build();

        technician = new Technician.TechnicianBuilder()
                .fullName("Tech Technician")
                .email("technician@example.com")
                .phoneNumber("0877123456")
                .password("encoded_password")
                .address("Jl. Teknisi No. 10")
                .experience("5 years in electronics")
                .build();
    }

    @Test
    void registerCustomer_ShouldCreateNewCustomer_WhenEmailIsUnique() {
        // Arrange
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(Customer.class))).thenReturn(customer);

        AuthResponse response = authService.registerCustomer(registerCustomerRequest);

        // Assert
        assertNotNull(response);
        assertNull(response.getToken());
        assertEquals("customer@example.com", response.getEmail());
        verify(userRepository).save(any(Customer.class));
    }

    @Test
    void registerCustomer_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerCustomer(registerCustomerRequest);
        });
        verify(userRepository, never()).save(any(Customer.class));
    }

    @Test
    void registerTechnician_ShouldCreateNewTechnician_WhenEmailIsUnique() {
        // Arrange
        when(userRepository.findByEmail("technician@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(Technician.class))).thenReturn(technician);
        when(jwtTokenService.generateToken("technician@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.registerTechnician(registerTechnicianRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("technician@example.com", response.getEmail());
        verify(userRepository).save(any(Technician.class));
    }

    @Test
    void registerTechnician_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail("technician@example.com")).thenReturn(Optional.of(technician));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerTechnician(registerTechnicianRequest);
        });
        verify(userRepository, never()).save(any(Technician.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("customer@example.com");
        when(jwtTokenService.generateToken(eq("customer@example.com"), anyMap())).thenReturn("jwt-token");
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));

        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("customer@example.com", response.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void logout_ShouldInvalidateToken() {
        // Arrange
        String token = "jwt-token";

        authService.logout(token);

        verify(jwtTokenService).invalidateToken(token);
    }
}
