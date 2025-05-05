package id.ac.ui.cs.advprog.kilimanjaro.service;

import com.google.protobuf.Empty;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenProvider;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GrpcAuthServiceImplTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StreamObserver<TokenValidationResponse> tokenValidationResponseObserver;

    @Mock
    private StreamObserver<HealthCheckResponse> healthCheckResponseObserver;

    private GrpcAuthServiceImpl grpcAuthServiceImpl;
    private String mockToken;
    private BaseUser mockBaseUser;
    private RequestMetadata mockMetadata;

    @BeforeEach
    void setUp() {
        grpcAuthServiceImpl = new GrpcAuthServiceImpl(jwtTokenProvider);
        mockToken = "valid.mock.token";
        mockBaseUser = createMockCustomer();
        mockMetadata = RequestMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setClientVersion("1.0.0")
                .build();
    }

    private Customer createMockCustomer() {
        Customer customer = spy(new Customer.CustomerBuilder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("1234567890")
                .password("password123")
                .address("123 Test Street")
                .build());

        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        return customer;
    }

    @Test
    void validateToken_WhenTokenValidWithoutUserData_ShouldReturnValidResponse() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setMetadata(mockMetadata)
                .setToken(mockToken)
                .setIncludeUserData(false)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertTrue(response.getValid());
        assertFalse(response.hasUserData());
        assertEquals(0, response.getStatus().getCode());
    }

    @Test
    void validateToken_WhenTokenValidWithUserData_ShouldReturnUserData() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setMetadata(mockMetadata)
                .setToken(mockToken)
                .setIncludeUserData(true)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertTrue(response.getValid());
        assertTrue(response.hasUserData());
        assertEquals(mockBaseUser.getId().toString(), response.getUserData().getIdentity().getId());
        assertEquals(0, response.getStatus().getCode());
    }

    @Test
    void validateToken_WhenTokenValidButUserNotFound_ShouldReturnInvalid() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setMetadata(mockMetadata)
                .setToken(mockToken)
                .setIncludeUserData(true)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(null);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertFalse(response.hasUserData());
        assertEquals(404, response.getStatus().getCode());
    }

    @Test
    void validateToken_WhenTokenInvalid_ShouldReturnInvalidResponse() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setMetadata(mockMetadata)
                .setToken(mockToken)
                .setIncludeUserData(false)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(false);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertEquals(401, response.getStatus().getCode());
    }

    @Test
    void validateToken_WhenExceptionThrown_ShouldReturnErrorResponse() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setMetadata(mockMetadata)
                .setToken(mockToken)
                .setIncludeUserData(false)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenThrow(new RuntimeException("Token validation failed"));

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertNotEquals(0, response.getStatus().getCode());
    }

    @Test
    void checkHealth_ShouldReturnServingStatus() {
        // Arrange
        Empty request = Empty.getDefaultInstance();

        // Act
        grpcAuthServiceImpl.checkHealth(request, healthCheckResponseObserver);

        // Assert
        ArgumentCaptor<HealthCheckResponse> responseCaptor = ArgumentCaptor.forClass(HealthCheckResponse.class);
        verify(healthCheckResponseObserver).onNext(responseCaptor.capture());
        verify(healthCheckResponseObserver).onCompleted();

        HealthCheckResponse response = responseCaptor.getValue();
        assertEquals(HealthCheckResponse.ServingStatus.SERVING_STATUS_SERVING, response.getStatus());
    }
}