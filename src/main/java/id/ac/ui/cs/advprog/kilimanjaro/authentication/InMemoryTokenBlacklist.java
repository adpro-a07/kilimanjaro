package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String token, long expiryEpochMillis) {
        blacklist.put(token, expiryEpochMillis);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Long expiry = blacklist.get(token);
        if (expiry == null) return false;

        if (expiry < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }
}

