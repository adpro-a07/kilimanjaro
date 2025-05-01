package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtTokenService {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    private final SigningKeyProvider keyProvider;
    private final JwtProperties properties;
    private final TokenBlacklist blacklist;
    private final Clock clock;

    public JwtTokenService(SigningKeyProvider keyProvider,
                           JwtProperties properties,
                           TokenBlacklist blacklist,
                           Clock clock) {
        this.keyProvider = Objects.requireNonNull(keyProvider, "SigningKeyProvider must not be null");
        this.properties = Objects.requireNonNull(properties, "JwtProperties must not be null");
        this.blacklist = Objects.requireNonNull(blacklist, "TokenBlacklist must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    /**
     * Generates a JWT token for the given username (without additional claims)
     *
     * @param username the username to include in the token
     * @return the generated JWT token
     * @throws IllegalArgumentException if username is null or empty
     */
    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    /**
     * Generates a JWT token for the given username with additional claims
     *
     * @param username the username to include in the token
     * @param additionalClaims a map of additional claims to include in the token
     * @return the generated JWT token
     * @throws IllegalArgumentException if username is null or empty
     */
    public String generateToken(String username, Map<String, Object> additionalClaims) {
        Assert.hasText(username, "Username must not be null or empty");

        Instant now = clock.instant();
        Instant expiry = now.plusMillis(properties.getExpiration());

        logger.debug("Generating token for user: {}", username);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(additionalClaims) // Add additional claims here
                .signWith(keyProvider.getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from a token
     *
     * @param token the JWT token
     * @return the username
     * @throws JwtException if the token is invalid or expired
     * @throws IllegalArgumentException if token is null or empty
     */
    public String extractUsername(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            return extractAllClaims(token).getSubject();
        } catch (JwtException e) {
            logger.warn("Failed to extract username from token", e);
            throw e;
        }
    }

    /**
     * Validates a token against user details
     *
     * @param token the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            logger.warn("Token or UserDetails is null during validation");
            return false;
        }

        try {
            if (blacklist.isBlacklisted(token)) {
                logger.info("Token validation failed: token is blacklisted");
                return false;
            }

            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            Instant expiration = claims.getExpiration().toInstant();

            boolean isValid = username.equals(userDetails.getUsername()) &&
                    expiration.isAfter(clock.instant());

            if (!isValid) {
                if (!username.equals(userDetails.getUsername())) {
                    logger.info("Token validation failed: username mismatch");
                } else {
                    logger.info("Token validation failed: token expired");
                }
            }

            return isValid;
        } catch (JwtException e) {
            logger.warn("Token validation failed due to JWT exception", e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Token validation failed due to invalid argument", e);
            return false;
        }
    }

    /**
     * Invalidates a token by adding it to the blacklist
     *
     * @param token the JWT token to invalidate
     * @throws JwtException if the token is invalid
     * @throws IllegalArgumentException if token is null or empty
     */
    public void invalidateToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            Claims claims = extractAllClaims(token);
            Instant expiry = claims.getExpiration().toInstant();
            blacklist.blacklist(token, expiry.toEpochMilli());
            logger.debug("Token invalidated and added to blacklist");
        } catch (JwtException e) {
            logger.warn("Failed to invalidate token", e);
            throw e;
        }
    }

    /**
     * Extracts a specific claim from the token
     *
     * @param token the JWT token
     * @param claimName the name of the claim to extract
     * @return the claim value
     * @throws JwtException if the token is invalid or expired
     * @throws IllegalArgumentException if token is null or empty
     */
    public <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        Assert.hasText(token, "Token must not be null or empty");
        Assert.hasText(claimName, "Claim name must not be null or empty");

        try {
            Claims claims = extractAllClaims(token);
            Object claimValue = claims.get(claimName);
            if (claimValue == null) {
                throw new IllegalArgumentException("Claim '" + claimName + "' not found in token");
            }
            return claims.get(claimName, claimType);
        } catch (JwtException e) {
            logger.warn("Failed to extract claim '{}' from token", claimName, e);
            throw e;
        }
    }

    /**
     * Extracts all claims from a token
     *
     * @param token the JWT token
     * @return the claims
     * @throws JwtException if the token is invalid
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyProvider.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}