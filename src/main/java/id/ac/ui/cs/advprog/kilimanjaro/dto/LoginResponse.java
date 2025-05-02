package id.ac.ui.cs.advprog.kilimanjaro.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String email;

    public LoginResponse() {
    }

    public LoginResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }
}
