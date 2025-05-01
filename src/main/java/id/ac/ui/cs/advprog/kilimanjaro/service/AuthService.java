package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.dto.AuthResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterCustomerRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterTechnicianRequest;

public interface AuthService {
    AuthResponse registerCustomer(RegisterCustomerRequest registerRequest);
    AuthResponse registerTechnician(RegisterTechnicianRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    void logout(String token);
}
