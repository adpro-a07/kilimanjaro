package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenProvider;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.*;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public GenericResponse<Void> registerCustomer(RegisterCustomerRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        Customer newCustomer = new Customer.CustomerBuilder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .address(registerRequest.getAddress())
                .build();

        userRepository.save(newCustomer);

        return new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Override
    public GenericResponse<Void> registerTechnician(RegisterTechnicianRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        Technician newTechnician = new Technician.TechnicianBuilder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .address(registerRequest.getAddress())
                .experience(registerRequest.getExperience())
                .build();

        userRepository.save(newTechnician);

        return new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Override
    public GenericResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            BaseUser user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

            String username = authentication.getName();
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());
            claims.put("fullName", user.getFullName());
            claims.put("id", user.getId());
            String token = jwtTokenProvider.generateAccessToken(username, claims);

            return new GenericResponse<>(
                    true,
                    "Login successful",
                    new LoginResponse(token, username)
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    public void logout(String token) {
        // Extract token from Bearer prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        jwtTokenProvider.invalidateToken(token);
    }
}
