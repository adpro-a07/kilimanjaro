package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderImpl.class);

    private final SigningKeyProvider keyProvider;
    private final UserRepository userRepository;
    private final JwtProperties properties;
    private final TokenBlacklist blacklist;
    private final Clock clock;

    public JwtTokenProviderImpl(SigningKeyProvider keyProvider,
                                UserRepository userRepository,
                                JwtProperties properties,
                                TokenBlacklist blacklist,
                                Clock clock) {
        this.keyProvider = Objects.requireNonNull(keyProvider);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.properties = Objects.requireNonNull(properties);
        this.blacklist = Objects.requireNonNull(blacklist);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    @Override
    public String generateToken(String username, Map<String, Object> additionalClaims) {
        Assert.hasText(username, "Username must not be null or empty");

        Instant now = clock.instant();
        Instant expiry = now.plusMillis(properties.getExpiration());

        logger.debug("Generating token for user: {}", username);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(additionalClaims)
                .signWith(keyProvider.getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            return extractAllClaims(token).getSubject();
        } catch (JwtException e) {
            logger.warn("Failed to extract username from token", e);
            throw e;
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (isEmpty(token)) return false;

        try {
            if (blacklist.isBlacklisted(token)) {
                logger.info("Token is blacklisted");
                return false;
            }

            Instant expiration = extractAllClaims(token).getExpiration().toInstant();
            return expiration.isAfter(clock.instant());
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token validation failed", e);
            return false;
        }
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        if (isEmpty(token) || userDetails == null) {
            logger.warn("Token or UserDetails is null during validation");
            return false;
        }

        try {
            if (blacklist.isBlacklisted(token)) {
                logger.info("Token is blacklisted");
                return false;
            }

            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            Instant expiration = claims.getExpiration().toInstant();

            boolean valid = username.equals(userDetails.getUsername()) &&
                    expiration.isAfter(clock.instant());

            if (!valid) {
                logger.info("Token validation failed: {}",
                        !username.equals(userDetails.getUsername()) ? "username mismatch" : "token expired");
            }

            return valid;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token validation failed", e);
            return false;
        }
    }

    @Override
    public void invalidateToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            Instant expiry = extractAllClaims(token).getExpiration().toInstant();
            blacklist.blacklist(token, expiry.toEpochMilli());
            logger.debug("Token invalidated and blacklisted");
        } catch (JwtException e) {
            logger.warn("Failed to invalidate token", e);
            throw e;
        }
    }

    @Override
    public BaseUser getUserFromToken(String token) throws AuthenticationException {
        if (isEmpty(token)) {
            throw new AuthenticationException("Token must not be null or empty");
        }

        try {
            if (blacklist.isBlacklisted(token)) {
                logger.warn("Attempt to retrieve user from blacklisted token");
                throw new AuthenticationException("Token is blacklisted");
            }

            Claims claims = extractAllClaims(token);
            if (claims.getExpiration().toInstant().isBefore(clock.instant())) {
                logger.warn("Attempt to retrieve user from expired token");
                throw new AuthenticationException("Token is expired");
            }

            String email = claims.getSubject();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("User not found for token subject"));
        } catch (JwtException e) {
            logger.warn("JWT parsing error", e);
            throw new AuthenticationException("Invalid JWT token", e);
        } catch (Exception e) {
            logger.warn("Unexpected error extracting user from token", e);
            throw new AuthenticationException("Authentication failed", e);
        }
    }


    @Override
    public <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        Assert.hasText(token, "Token must not be null or empty");
        Assert.hasText(claimName, "Claim name must not be null or empty");

        try {
            Claims claims = extractAllClaims(token);
            Object value = claims.get(claimName);
            if (value == null) {
                throw new IllegalArgumentException("Claim '" + claimName + "' not found in token");
            }
            return claims.get(claimName, claimType);
        } catch (JwtException e) {
            logger.warn("Failed to extract claim '{}' from token", claimName, e);
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyProvider.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
