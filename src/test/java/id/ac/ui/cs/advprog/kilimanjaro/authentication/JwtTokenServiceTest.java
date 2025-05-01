package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {
    @Mock
    private SigningKeyProvider keyProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenBlacklist tokenBlacklist;

    private JwtTokenService jwtTokenService;
    private Clock fixedClock;
    private Key testKey;

    private final String TEST_USERNAME = "testuser";
    private final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        testKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        jwtTokenService = new JwtTokenService(
                keyProvider,
                jwtProperties,
                tokenBlacklist,
                fixedClock
        );
    }

    // ========== Token Generation Tests ==========
    @Test
    void generateToken_WithValidUsername_ReturnsToken() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);

        String token = jwtTokenService.generateToken(TEST_USERNAME);

        assertNotNull(token);
        Claims claims = parseToken(token);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void generateToken_WithAdditionalClaims_IncludesClaimsInToken() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);

        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("userId", 123);
        additionalClaims.put("role", "ADMIN");
        additionalClaims.put("email", "test@example.com");

        String token = jwtTokenService.generateToken(TEST_USERNAME, additionalClaims);

        Claims claims = parseToken(token);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertEquals(123, claims.get("userId"));
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("test@example.com", claims.get("email"));
    }

    @Test
    void generateToken_WithNullAdditionalClaims_WorksCorrectly() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);

        String token = jwtTokenService.generateToken(TEST_USERNAME, null);

        Claims claims = parseToken(token);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertTrue(claims.size() >= 3); // sub, iat, exp at minimum
    }

    @Test
    void generateToken_WithNullUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.generateToken(null));
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.generateToken(null, new HashMap<>()));
    }

    @Test
    void generateToken_WithEmptyUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.generateToken(""));
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.generateToken("", new HashMap<>()));
    }

    // ========== Claim Extraction Tests ==========
    @Test
    void extractClaim_WithValidTokenAndClaim_ReturnsClaimValue() {
        when(keyProvider.getKey()).thenReturn(testKey);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123);
        claims.put("active", true);

        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .addClaims(claims)
                .signWith(testKey)
                .compact();

        assertEquals(123, jwtTokenService.extractClaim(token, "userId", Integer.class));
        assertEquals(true, jwtTokenService.extractClaim(token, "active", Boolean.class));
    }

    @Test
    void extractClaim_WithNonExistentClaim_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);

        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .signWith(testKey)
                .compact();

        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.extractClaim(token, "nonExistent", String.class));
    }

    @Test
    void extractClaim_WithInvalidToken_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);
        assertThrows(JwtException.class,
                () -> jwtTokenService.extractClaim("invalid.token", "claim", String.class));
    }

    @Test
    void extractClaim_WithNullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.extractClaim(null, "claim", String.class));
    }

    @Test
    void extractClaim_WithNullClaimName_ThrowsException() {
        String token = Jwts.builder().signWith(testKey).compact();

        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenService.extractClaim(token, null, String.class));
    }

    // ========== Username Extraction Tests ==========
    @Test
    void extractUsername_WithValidToken_ReturnsUsername() {
        when(keyProvider.getKey()).thenReturn(testKey);

        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .signWith(testKey)
                .compact();

        assertEquals(TEST_USERNAME, jwtTokenService.extractUsername(token));
    }

    @Test
    void extractUsername_WithInvalidToken_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);
        assertThrows(JwtException.class, () -> jwtTokenService.extractUsername("invalid.token"));
    }

    @Test
    void extractUsername_WithNullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenService.extractUsername(null));
    }

    // ========== Token Validation Tests ==========
    @Test
    void validateToken_WithValidTokenAndUser_ReturnsTrue() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);
        when(tokenBlacklist.isBlacklisted(anyString())).thenReturn(false);

        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String validToken = jwtTokenService.generateToken(TEST_USERNAME);

        assertTrue(jwtTokenService.validateToken(validToken, userDetails));
    }

    @Test
    void validateToken_WithAdditionalClaims_StillValidatesCorrectly() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);
        when(tokenBlacklist.isBlacklisted(anyString())).thenReturn(false);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123);

        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String token = jwtTokenService.generateToken(TEST_USERNAME, claims);

        assertTrue(jwtTokenService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_WithBlacklistedToken_ReturnsFalse() {
        when(keyProvider.getKey()).thenReturn(testKey);
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String token = jwtTokenService.generateToken(TEST_USERNAME);

        when(tokenBlacklist.isBlacklisted(token)).thenReturn(true);

        assertFalse(jwtTokenService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());

        // Create token with past expiration
        Instant pastDate = Instant.now(fixedClock).minusSeconds(EXPIRATION_MS + 1000);
        String expiredToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(Date.from(pastDate))
                .signWith(testKey)
                .compact();

        when(tokenBlacklist.isBlacklisted(expiredToken)).thenReturn(false);

        boolean isValid = jwtTokenService.validateToken(expiredToken, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithWrongUser_ReturnsFalse() {
        when(keyProvider.getKey()).thenReturn(testKey);
        UserDetails wrongUser = new User("wronguser", "password", Collections.emptyList());
        String validToken = jwtTokenService.generateToken(TEST_USERNAME);

        when(tokenBlacklist.isBlacklisted(validToken)).thenReturn(false);

        boolean isValid = jwtTokenService.validateToken(validToken, wrongUser);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullToken_ReturnsFalse() {
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());

        boolean isValid = jwtTokenService.validateToken(null, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullUserDetails_ReturnsFalse() {
        when(keyProvider.getKey()).thenReturn(testKey);
        String validToken = jwtTokenService.generateToken(TEST_USERNAME);

        boolean isValid = jwtTokenService.validateToken(validToken, null);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String invalidToken = "invalid.token";

        boolean isValid = jwtTokenService.validateToken(invalidToken, userDetails);

        assertFalse(isValid);
    }

    // ========== Token Invalidation Tests ==========

    @Test
    void invalidateToken_WithValidToken_AddsToBlacklist() {
        when(keyProvider.getKey()).thenReturn(testKey);

        Instant expiration = Instant.now(fixedClock).plusMillis(EXPIRATION_MS);
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(Date.from(expiration))
                .signWith(testKey)
                .compact();

        jwtTokenService.invalidateToken(token);

        // Allow for small differences in time (up to 1 second)
        verify(tokenBlacklist).blacklist(
                eq(token),
                longThat(actual -> Math.abs(actual - expiration.toEpochMilli()) < 1000)
        );
    }

    @Test
    void invalidateToken_WithNullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenService.invalidateToken(null));
    }

    @Test
    void invalidateToken_WithEmptyToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenService.invalidateToken(""));
    }

    @Test
    void invalidateToken_WithInvalidToken_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);

        String invalidToken = "invalid.token";

        assertThrows(JwtException.class, () -> jwtTokenService.invalidateToken(invalidToken));
    }

    @Test
    void invalidateToken_WithExpiredToken_StillAddsToBlacklist() {
        when(keyProvider.getKey()).thenReturn(testKey);
        Instant pastDate = Instant.now(fixedClock).minusSeconds(EXPIRATION_MS + 1000);

        String expiredToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(Date.from(pastDate))
                .signWith(testKey)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtTokenService.invalidateToken(expiredToken));
    }

    // ========== Helper Methods ==========
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(testKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}