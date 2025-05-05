package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtTokenProvider {
    String generateToken(String username);

    String generateToken(String username, Map<String, Object> additionalClaims);

    String extractUsername(String token);

    boolean validateToken(String token);

    boolean validateToken(String token, UserDetails userDetails);

    void invalidateToken(String token);

    BaseUser getUserFromToken(String token) throws AuthenticationException;

    <T> T extractClaim(String token, String claimName, Class<T> claimType);
}
