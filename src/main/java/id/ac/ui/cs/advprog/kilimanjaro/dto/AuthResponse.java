package id.ac.ui.cs.advprog.kilimanjaro.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }
}
