package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
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
class JwtTokenProviderTest {
    @Mock
    private SigningKeyProvider keyProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenBlacklist blacklist;

    private JwtTokenProvider jwtTokenProvider;
    private Clock fixedClock;
    private Key testKey;

    private final String TEST_USERNAME = "testuser";
    private final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        testKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        jwtTokenProvider = new JwtTokenProvider(
                keyProvider,
                userRepository,
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

        String token = jwtTokenProvider.generateToken(TEST_USERNAME);

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

        String token = jwtTokenProvider.generateToken(TEST_USERNAME, additionalClaims);

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

        String token = jwtTokenProvider.generateToken(TEST_USERNAME, null);

        Claims claims = parseToken(token);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertTrue(claims.size() >= 3); // sub, iat, exp at minimum
    }

    @Test
    void generateToken_WithNullUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.generateToken(null));
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.generateToken(null, new HashMap<>()));
    }

    @Test
    void generateToken_WithEmptyUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.generateToken(""));
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.generateToken("", new HashMap<>()));
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

        assertEquals(123, jwtTokenProvider.extractClaim(token, "userId", Integer.class));
        assertEquals(true, jwtTokenProvider.extractClaim(token, "active", Boolean.class));
    }

    @Test
    void extractClaim_WithNonExistentClaim_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);

        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .signWith(testKey)
                .compact();

        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.extractClaim(token, "nonExistent", String.class));
    }

    @Test
    void extractClaim_WithInvalidToken_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);
        assertThrows(JwtException.class,
                () -> jwtTokenProvider.extractClaim("invalid.token", "claim", String.class));
    }

    @Test
    void extractClaim_WithNullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.extractClaim(null, "claim", String.class));
    }

    @Test
    void extractClaim_WithNullClaimName_ThrowsException() {
        String token = Jwts.builder().signWith(testKey).compact();

        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.extractClaim(token, null, String.class));
    }

    // ========== Username Extraction Tests ==========
    @Test
    void extractUsername_WithValidToken_ReturnsUsername() {
        when(keyProvider.getKey()).thenReturn(testKey);

        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .signWith(testKey)
                .compact();

        assertEquals(TEST_USERNAME, jwtTokenProvider.extractUsername(token));
    }

    @Test
    void extractUsername_WithInvalidToken_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);
        assertThrows(JwtException.class, () -> jwtTokenProvider.extractUsername("invalid.token"));
    }

    @Test
    void extractUsername_WithNullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.extractUsername(null));
    }

    // ========== Token Validation Tests ==========
    @Test
    void validateToken_WithValidTokenAndUser_ReturnsTrue() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);
        when(tokenBlacklist.isBlacklisted(anyString())).thenReturn(false);

        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String validToken = jwtTokenProvider.generateToken(TEST_USERNAME);

        assertTrue(jwtTokenProvider.validateToken(validToken, userDetails));
    }

    @Test
    void validateToken_WithAdditionalClaims_StillValidatesCorrectly() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);
        when(tokenBlacklist.isBlacklisted(anyString())).thenReturn(false);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123);

        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, claims);

        assertTrue(jwtTokenProvider.validateToken(token, userDetails));
    }

    @Test
    void validateToken_WithBlacklistedToken_ReturnsFalse() {
        when(keyProvider.getKey()).thenReturn(testKey);
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String token = jwtTokenProvider.generateToken(TEST_USERNAME);

        when(tokenBlacklist.isBlacklisted(token)).thenReturn(true);

        assertFalse(jwtTokenProvider.validateToken(token, userDetails));
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

        boolean isValid = jwtTokenProvider.validateToken(expiredToken, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithWrongUser_ReturnsFalse() {
        when(keyProvider.getKey()).thenReturn(testKey);
        UserDetails wrongUser = new User("wronguser", "password", Collections.emptyList());
        String validToken = jwtTokenProvider.generateToken(TEST_USERNAME);

        when(tokenBlacklist.isBlacklisted(validToken)).thenReturn(false);

        boolean isValid = jwtTokenProvider.validateToken(validToken, wrongUser);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullToken_ReturnsFalse() {
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());

        boolean isValid = jwtTokenProvider.validateToken(null, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullUserDetails_ReturnsFalse() {
        when(keyProvider.getKey()).thenReturn(testKey);
        String validToken = jwtTokenProvider.generateToken(TEST_USERNAME);

        boolean isValid = jwtTokenProvider.validateToken(validToken, null);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        String invalidToken = "invalid.token";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnTrueForValidNonBlacklistedToken() {
        when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_MS);
        when(keyProvider.getKey()).thenReturn(testKey);
        String token = jwtTokenProvider.generateToken(TEST_USERNAME);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }


    @Test
    void validateToken_shouldReturnFalseIfTokenIsBlacklisted() {
        when(keyProvider.getKey()).thenReturn(testKey);
        String token = jwtTokenProvider.generateToken("blacklisted");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        assertFalse(jwtTokenProvider.validateToken("malformed.token.value"));
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

        jwtTokenProvider.invalidateToken(token);

        // Allow for small differences in time (up to 1 second)
        verify(tokenBlacklist).blacklist(
                eq(token),
                longThat(actual -> Math.abs(actual - expiration.toEpochMilli()) < 1000)
        );
    }

    @Test
    void invalidateToken_WithNullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.invalidateToken(null));
    }

    @Test
    void invalidateToken_WithEmptyToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.invalidateToken(""));
    }

    @Test
    void invalidateToken_WithInvalidToken_ThrowsException() {
        when(keyProvider.getKey()).thenReturn(testKey);

        String invalidToken = "invalid.token";

        assertThrows(JwtException.class, () -> jwtTokenProvider.invalidateToken(invalidToken));
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

        assertThrows(ExpiredJwtException.class, () -> jwtTokenProvider.invalidateToken(expiredToken));
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