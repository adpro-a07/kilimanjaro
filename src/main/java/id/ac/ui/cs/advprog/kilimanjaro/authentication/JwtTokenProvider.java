package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface JwtTokenProvider {
    /**
     * Record to represent a pair of access and refresh tokens
     */
    String generateAccessToken(String username, Map<String, Object> additionalClaims);

    String generateRefreshToken(String username, Map<String, Object> additionalClaims);

    String getEmailFromToken(String token);

    UUID getUserIdFromToken(String token);

    String getTokenType(String token);

    boolean validateToken(String token);

    void invalidateToken(String token);

    <T> T getClaimFromToken(String token, String claimName, Class<T> claimType);

    <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver);

    Date getExpirationDateFromToken(String token);
}
