package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenProvider;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenServiceImplTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;
    private JwtTokenServiceImpl jwtTokenService;

    private final String email = "user@example.com";
    private final String token = "valid.jwt.token";
    private final UUID userId = UUID.randomUUID();
    private final BaseUser user = mock(BaseUser.class);

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userRepository = mock(UserRepository.class);
        jwtTokenService = new JwtTokenServiceImpl(jwtTokenProvider, userRepository);

        when(user.getId()).thenReturn(userId);
        when(user.getFullName()).thenReturn("User Name");
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user.getEmail()).thenReturn(email);
    }

    // validateToken

    @Test
    void testValidateTokenReturnsFalseIfValidAndAllClaimsPresent() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenProvider.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenProvider.getClaimFromToken(token, "userId", UUID.class)).thenReturn(userId);

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void testValidateTokenReturnsTrueIfProviderSaysInvalid() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void testValidateTokenReturnsTrueIfClaimsMissing() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClaimFromToken(token, "role", String.class)).thenReturn(null);

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void testValidateTokenReturnsTrueIfExceptionThrown() {
        when(jwtTokenProvider.validateToken(token)).thenThrow(RuntimeException.class);

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isTrue();
    }

    // refreshToken

    @Test
    void testRefreshTokenReturnsNewTokensIfValid() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenProvider.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenProvider.getClaimFromToken(token, "userId", UUID.class)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(eq(email), anyMap())).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(eq(email), anyMap())).thenReturn("new-refresh");

        JwtTokenService.TokenPair tokens = jwtTokenService.refreshToken(token);

        assertThat(tokens.accessToken()).isEqualTo("new-access");
        assertThat(tokens.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void testRefreshTokenThrowsIfTokenInvalid() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> jwtTokenService.refreshToken(token))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid refresh token");
    }

    // getUserFromToken

    @Test
    void testGetUserFromTokenReturnsUserIfValid() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenProvider.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenProvider.getClaimFromToken(token, "userId", UUID.class)).thenReturn(userId);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BaseUser result = jwtTokenService.getUserFromToken(token);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void testGetUserFromTokenThrowsIfInvalidToken() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> jwtTokenService.getUserFromToken(token))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testGetUserFromTokenThrowsIfUserNotFound() {
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClaimFromToken(token, "role", String.class)).thenReturn("ROLE_USER");
        when(jwtTokenProvider.getClaimFromToken(token, "fullName", String.class)).thenReturn("User Name");
        when(jwtTokenProvider.getClaimFromToken(token, "userId", UUID.class)).thenReturn(userId);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtTokenService.getUserFromToken(token))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User not found in token");
    }

    // invalidateToken

    @Test
    void testInvalidateTokenCallsProvider() {
        jwtTokenService.invalidateToken(token);

        verify(jwtTokenProvider).invalidateToken(token);
    }

    @Test
    void testInvalidateTokenHandlesExceptionGracefully() {
        doThrow(new RuntimeException("Fail")).when(jwtTokenProvider).invalidateToken(token);

        jwtTokenService.invalidateToken(token); // should not throw
    }

    // generateTokensFromEmail

    @Test
    void testGenerateTokensFromEmailReturnsTokens() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(eq(email), anyMap())).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(eq(email), anyMap())).thenReturn("refresh");

        JwtTokenService.TokenPair pair = jwtTokenService.generateTokensFromEmail(email);

        assertThat(pair.accessToken()).isEqualTo("access");
        assertThat(pair.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void testGenerateTokensFromEmailThrowsIfUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtTokenService.generateTokensFromEmail(email))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email");
    }
}

