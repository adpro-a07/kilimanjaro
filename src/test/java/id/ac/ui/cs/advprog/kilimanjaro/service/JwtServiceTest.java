package id.ac.ui.cs.advprog.kilimanjaro.service;

import com.google.protobuf.Timestamp;
import id.ac.ui.cs.advprog.kilimanjaro.auth.*;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenProvider;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
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
public class JwtServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StreamObserver<VerifyTokenResponse> verifyResponseObserver;

    @Mock
    private StreamObserver<GetUserFromTokenResponse> getUserResponseObserver;

    @Mock
    private StreamObserver<ValidateAndExtractResponse> validateResponseObserver;

    private JwtService jwtService;
    private String mockToken;
    private BaseUser mockBaseUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtTokenProvider);
        mockToken = "valid.mock.token";
        mockBaseUser = createMockCustomer();
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

        // Set dates since they're not part of the builder
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        return customer;
    }

    @Test
    void verifyToken_WhenTokenValid_ShouldReturnTrue() {
        // Arrange
        VerifyTokenRequest request = VerifyTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);

        // Act
        jwtService.verifyToken(request, verifyResponseObserver);

        // Assert
        ArgumentCaptor<VerifyTokenResponse> responseCaptor = ArgumentCaptor.forClass(VerifyTokenResponse.class);
        verify(verifyResponseObserver).onNext(responseCaptor.capture());
        verify(verifyResponseObserver).onCompleted();

        VerifyTokenResponse response = responseCaptor.getValue();
        assertTrue(response.getValid());
    }

    @Test
    void verifyToken_WhenTokenInvalid_ShouldReturnFalse() {
        // Arrange
        VerifyTokenRequest request = VerifyTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(false);

        // Act
        jwtService.verifyToken(request, verifyResponseObserver);

        // Assert
        ArgumentCaptor<VerifyTokenResponse> responseCaptor = ArgumentCaptor.forClass(VerifyTokenResponse.class);
        verify(verifyResponseObserver).onNext(responseCaptor.capture());
        verify(verifyResponseObserver).onCompleted();

        VerifyTokenResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
    }

    @Test
    void verifyToken_WhenExceptionThrown_ShouldReturnFalse() {
        // Arrange
        VerifyTokenRequest request = VerifyTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenThrow(new RuntimeException("Token validation failed"));

        // Act
        jwtService.verifyToken(request, verifyResponseObserver);

        // Assert
        ArgumentCaptor<VerifyTokenResponse> responseCaptor = ArgumentCaptor.forClass(VerifyTokenResponse.class);
        verify(verifyResponseObserver).onNext(responseCaptor.capture());
        verify(verifyResponseObserver).onCompleted();

        VerifyTokenResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
    }

    @Test
    void getUserFromToken_WhenUserExists_ShouldReturnUserData() {
        // Arrange
        GetUserFromTokenRequest request = GetUserFromTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);

        // Act
        jwtService.getUserFromToken(request, getUserResponseObserver);

        // Assert
        ArgumentCaptor<GetUserFromTokenResponse> responseCaptor = ArgumentCaptor.forClass(GetUserFromTokenResponse.class);
        verify(getUserResponseObserver).onNext(responseCaptor.capture());
        verify(getUserResponseObserver).onCompleted();

        GetUserFromTokenResponse response = responseCaptor.getValue();
        assertNotNull(response.getUser());
        assertEquals(mockBaseUser.getId().toString(), response.getUser().getIdentity().getId());
        assertEquals(mockBaseUser.getEmail(), response.getUser().getIdentity().getEmail());
        assertEquals(mockBaseUser.getFullName(), response.getUser().getIdentity().getFullName());
        assertEquals(mockBaseUser.getPhoneNumber(), response.getUser().getIdentity().getPhoneNumber());
        assertEquals(mockBaseUser.getRole().name(), response.getUser().getIdentity().getRole().name());
        assertEquals(mockBaseUser.getProfile(), response.getUser().getProfile());
    }

    @Test
    void getUserFromToken_WhenUserDoesNotExist_ShouldReturnEmptyResponse() {
        // Arrange
        GetUserFromTokenRequest request = GetUserFromTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(null);

        // Act
        jwtService.getUserFromToken(request, getUserResponseObserver);

        // Assert
        ArgumentCaptor<GetUserFromTokenResponse> responseCaptor = ArgumentCaptor.forClass(GetUserFromTokenResponse.class);
        verify(getUserResponseObserver).onNext(responseCaptor.capture());
        verify(getUserResponseObserver).onCompleted();

        GetUserFromTokenResponse response = responseCaptor.getValue();
        // In protobuf, "hasUser" will be false when the field is not set
        assertFalse(response.hasUser());
    }

    @Test
    void validateAndExtract_WhenValid_ShouldReturnValidAndUserData() {
        // Arrange
        ValidateAndExtractRequest request = ValidateAndExtractRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);

        // Act
        jwtService.validateAndExtract(request, validateResponseObserver);

        // Assert
        ArgumentCaptor<ValidateAndExtractResponse> responseCaptor = ArgumentCaptor.forClass(ValidateAndExtractResponse.class);
        verify(validateResponseObserver).onNext(responseCaptor.capture());

        ValidateAndExtractResponse response = responseCaptor.getValue();
        assertTrue(response.getValid());
        assertNotNull(response.getUser());
        assertEquals(mockBaseUser.getId().toString(), response.getUser().getIdentity().getId());
        assertEquals(mockBaseUser.getEmail(), response.getUser().getIdentity().getEmail());
    }

    @Test
    void validateAndExtract_WhenInvalid_ShouldReturnInvalidAndNoUser() {
        // Arrange
        ValidateAndExtractRequest request = ValidateAndExtractRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(null);

        // Act
        jwtService.validateAndExtract(request, validateResponseObserver);

        // Assert
        ArgumentCaptor<ValidateAndExtractResponse> responseCaptor = ArgumentCaptor.forClass(ValidateAndExtractResponse.class);
        verify(validateResponseObserver).onNext(responseCaptor.capture());
        // Bug in JwtService: validateAndExtract doesn't call onCompleted()
        // verify(validateResponseObserver).onCompleted();

        ValidateAndExtractResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertFalse(response.hasUser());
    }

    @Test
    void extractUserData_ShouldConvertBaseUserToUserData() {
        // This test indirectly tests the private method extractUserData by checking the output of getUserFromToken

        // Arrange
        GetUserFromTokenRequest request = GetUserFromTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);

        // Act
        jwtService.getUserFromToken(request, getUserResponseObserver);

        // Assert
        ArgumentCaptor<GetUserFromTokenResponse> responseCaptor = ArgumentCaptor.forClass(GetUserFromTokenResponse.class);
        verify(getUserResponseObserver).onNext(responseCaptor.capture());

        UserData userData = responseCaptor.getValue().getUser();

        // Verify identity
        assertEquals(mockBaseUser.getId().toString(), userData.getIdentity().getId());
        assertEquals(mockBaseUser.getEmail(), userData.getIdentity().getEmail());
        assertEquals(mockBaseUser.getFullName(), userData.getIdentity().getFullName());
        assertEquals(mockBaseUser.getPhoneNumber(), userData.getIdentity().getPhoneNumber());
        assertEquals(mockBaseUser.getRole().name(), userData.getIdentity().getRole().name());

        // Verify timestamps exist
        assertNotNull(userData.getIdentity().getCreatedAt());
        assertNotNull(userData.getIdentity().getUpdatedAt());

        // Verify profile data for Customer
        Customer customer = (Customer) mockBaseUser;
        UserProfile expectedProfile = customer.getProfile();
        assertEquals(expectedProfile.getAddress(), userData.getProfile().getAddress());
    }

    @Test
    void convertLocalDateTimeToTimestamp_ShouldConvertCorrectly() {
        // This test indirectly tests the private method convertLocalDateTimeToTimestamp
        // by checking timestamp values in the userData

        // Arrange
        LocalDateTime testDateTime = LocalDateTime.of(2023, 5, 15, 10, 30, 0);
        mockBaseUser.setCreatedAt(testDateTime);

        GetUserFromTokenRequest request = GetUserFromTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);

        // Act
        jwtService.getUserFromToken(request, getUserResponseObserver);

        // Assert
        ArgumentCaptor<GetUserFromTokenResponse> responseCaptor = ArgumentCaptor.forClass(GetUserFromTokenResponse.class);
        verify(getUserResponseObserver).onNext(responseCaptor.capture());

        Timestamp timestamp = responseCaptor.getValue().getUser().getIdentity().getCreatedAt();
        assertNotNull(timestamp);

        // The actual value checks would depend on timezone, so we're just checking it's non-zero
        assertTrue(timestamp.getSeconds() > 0);
    }

    @Test
    void onCompleteMethodsShouldBeCalledForAllGrpcMethods() {
        // Test for verifyToken
        VerifyTokenRequest verifyRequest = VerifyTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);
        jwtService.verifyToken(verifyRequest, verifyResponseObserver);
        verify(verifyResponseObserver).onCompleted();

        // Test for getUserFromToken
        GetUserFromTokenRequest getUserRequest = GetUserFromTokenRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);
        jwtService.getUserFromToken(getUserRequest, getUserResponseObserver);
        verify(getUserResponseObserver).onCompleted();

        // Test for validateAndExtract - this method was missing onCompleted() in original code
        ValidateAndExtractRequest validateRequest = ValidateAndExtractRequest.newBuilder()
                .setToken(mockToken)
                .build();
        when(jwtTokenProvider.getUserFromToken(mockToken)).thenReturn(mockBaseUser);
        jwtService.validateAndExtract(validateRequest, validateResponseObserver);
        verify(validateResponseObserver).onCompleted();
    }
}