package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTokenBlacklistTest {

    private InMemoryTokenBlacklist blacklist;
    private final String TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        blacklist = new InMemoryTokenBlacklist();
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenTokenNotInBlacklist() {
        // When
        boolean result = blacklist.isBlacklisted(TOKEN);

        // Then
        assertFalse(result);
    }

    @Test
    void isBlacklisted_ShouldReturnTrue_WhenTokenInBlacklistAndNotExpired() {
        // Given
        long futureExpiryTime = Instant.now().plusSeconds(60).toEpochMilli();
        blacklist.blacklist(TOKEN, futureExpiryTime);

        // When
        boolean result = blacklist.isBlacklisted(TOKEN);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_WhenTokenInBlacklistButExpired() {
        // Given
        long pastExpiryTime = Instant.now().minusSeconds(60).toEpochMilli();
        blacklist.blacklist(TOKEN, pastExpiryTime);

        // When
        boolean result = blacklist.isBlacklisted(TOKEN);

        // Then
        assertFalse(result);
    }

    @Test
    void isBlacklisted_ShouldRemoveExpiredToken_WhenChecked() {
        // Given
        long pastExpiryTime = Instant.now().minusSeconds(60).toEpochMilli();
        blacklist.blacklist(TOKEN, pastExpiryTime);

        // When
        blacklist.isBlacklisted(TOKEN);

        // Then - Try a second check to verify it was removed
        boolean secondCheck = blacklist.isBlacklisted(TOKEN);
        assertFalse(secondCheck);
    }

    @Test
    void blacklist_ShouldOverwriteExistingEntry_WhenSameTokenAddedTwice() {
        // Given
        long firstExpiryTime = Instant.now().plusSeconds(30).toEpochMilli();
        long secondExpiryTime = Instant.now().plusSeconds(60).toEpochMilli();

        // When
        blacklist.blacklist(TOKEN, firstExpiryTime);
        blacklist.blacklist(TOKEN, secondExpiryTime);

        // Then
        // We need to test indirectly by waiting for the first expiry time to pass
        // and ensuring the token is still blacklisted
        try {
            // Wait until slightly after the first expiry time
            TimeUnit.MILLISECONDS.sleep(firstExpiryTime - System.currentTimeMillis() + 100);

            // The token should still be blacklisted if the second expiry time was used
            boolean result = blacklist.isBlacklisted(TOKEN);
            assertTrue(result);
        } catch (InterruptedException e) {
            fail("Test interrupted while waiting for expiry time");
        } catch (IllegalArgumentException e) {
            // If current time is already past the first expiry, we can't test this scenario reliably in this way
            // So we'll skip the assertion and pass the test
            System.out.println("Skipping assertion due to timing constraints: " + e.getMessage());
        }
    }

    @Test
    void blacklist_ShouldAcceptMultipleTokens() {
        // Given
        String token1 = "token.1";
        String token2 = "token.2";
        long futureExpiryTime = Instant.now().plusSeconds(60).toEpochMilli();

        // When
        blacklist.blacklist(token1, futureExpiryTime);
        blacklist.blacklist(token2, futureExpiryTime);

        // Then
        assertTrue(blacklist.isBlacklisted(token1));
        assertTrue(blacklist.isBlacklisted(token2));
    }

    @Test
    void isBlacklisted_ShouldHandleNullToken() {
        // When & Then
        assertFalse(blacklist.isBlacklisted(null));
    }
}
