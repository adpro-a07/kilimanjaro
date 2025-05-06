package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserNotFoundException;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapper;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapperFactory;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserMapperServiceImplTest {

    private UserRepository userRepository;
    private UserDataMapperFactory mapperFactory;
    private JwtTokenService jwtTokenService;
    private UserMapperServiceImpl userMapperService;

    private BaseUser user;
    private UserData userData;
    private UserDataMapper<BaseUser> mapper;
    private final UUID userId = UUID.randomUUID();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        mapperFactory = mock(UserDataMapperFactory.class);
        jwtTokenService = mock(JwtTokenService.class);
        userMapperService = new UserMapperServiceImpl(userRepository, mapperFactory, jwtTokenService);

        user = mock(BaseUser.class);
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);

        userData = UserData.newBuilder().build();

        mapper = (UserDataMapper<BaseUser>) mock(UserDataMapper.class);
    }

    // --- getUserById(UUID) ---
    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user)).thenReturn(userData);

        UserData result = userMapperService.getUserById(userId);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserById(userId));
    }

    // --- getUserById(UUID, includeProfile) ---
    @Test
    void testGetUserById_WithIncludeProfileTrue() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, true)).thenReturn(userData);

        UserData result = userMapperService.getUserById(userId, true);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserById_WithIncludeProfileFalse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, false)).thenReturn(userData);

        UserData result = userMapperService.getUserById(userId, false);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserById_WithIncludeProfile_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserById(userId, true));
    }

    // --- getUserByEmail(String) ---
    @Test
    void testGetUserByEmail_Success() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user)).thenReturn(userData);

        UserData result = userMapperService.getUserByEmail(email);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserByEmail_UserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserByEmail("missing@example.com"));
    }

    // --- getUserByEmail(String, includeProfile) ---
    @Test
    void testGetUserByEmail_WithIncludeProfileTrue() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, true)).thenReturn(userData);

        UserData result = userMapperService.getUserByEmail(email, true);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserByEmail_WithIncludeProfileFalse() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, false)).thenReturn(userData);

        UserData result = userMapperService.getUserByEmail(email, false);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserByEmail_WithIncludeProfile_UserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserByEmail("missing@example.com", true));
    }

    // --- getUserDataFromToken ---
    @Test
    void testGetUserDataFromToken_Success() throws AuthenticationException, UserNotFoundException {
        String token = "valid.token.here";
        when(jwtTokenService.getUserFromToken(token)).thenReturn(user);
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user)).thenReturn(userData);

        UserData result = userMapperService.getUserDataFromToken(token);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserDataFromToken_UserNotFoundInToken() {
        String token = "invalid.token";
        when(jwtTokenService.getUserFromToken(token)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserDataFromToken(token));
    }

    @Test
    void testGetUserDataFromToken_ThrowsAuthenticationException() throws AuthenticationException {
        String token = "bad.token";
        when(jwtTokenService.getUserFromToken(token)).thenThrow(new AuthenticationException("Invalid"));

        assertThrows(AuthenticationException.class, () -> userMapperService.getUserDataFromToken(token));
    }
}
