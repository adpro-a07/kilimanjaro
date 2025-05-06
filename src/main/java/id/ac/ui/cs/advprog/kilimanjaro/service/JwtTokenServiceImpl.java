package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenProvider;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_FULL_NAME = "fullName";
    private static final String CLAIM_USER_ID = "userId";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtTokenServiceImpl(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return true;
            }

            // Ensure all additional claims are present
            String role = jwtTokenProvider.getClaimFromToken(token, CLAIM_ROLE, String.class);
            String fullName = jwtTokenProvider.getClaimFromToken(token, CLAIM_FULL_NAME, String.class);
            UUID userId = jwtTokenProvider.getClaimFromToken(token, CLAIM_USER_ID, UUID.class);

            if (role == null || fullName == null || userId == null) {
                logger.warn("Token claims are missing or invalid");
                return true;
            }

            return false;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public TokenPair refreshToken(String refreshToken) throws AuthenticationException {
        if (validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        return generateTokensFromEmail(email);
    }

    @Override
    public BaseUser getUserFromToken(String token) throws AuthenticationException {
        if (validateToken(token)) {
            throw new AuthenticationException("Invalid token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found in token"));
    }

    @Override
    public void invalidateToken(String token) {
        try {
            jwtTokenProvider.invalidateToken(token);
        } catch (Exception e) {
            logger.error("Failed to invalidate token: {}", e.getMessage());
        }
    }

    @Override
    public TokenPair generateTokensFromEmail(String email) {
        BaseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROLE, user.getRole());
        claims.put(CLAIM_FULL_NAME, user.getFullName());
        claims.put(CLAIM_USER_ID, user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(email, claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, claims);

        return new TokenPair(accessToken, refreshToken);
    }
}
