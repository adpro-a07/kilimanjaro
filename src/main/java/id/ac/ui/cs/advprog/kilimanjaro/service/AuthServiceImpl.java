package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenService;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.AuthResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterCustomerRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterTechnicianRequest;
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
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse registerCustomer(RegisterCustomerRequest registerRequest) {
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

        return new AuthResponse(null, newCustomer.getEmail());
    }

    @Override
    public AuthResponse registerTechnician(RegisterTechnicianRequest registerRequest) {
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

        String token = jwtTokenService.generateToken(newTechnician.getEmail());

        return new AuthResponse(token, newTechnician.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
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
            String token = jwtTokenService.generateToken(username, claims);

            return new AuthResponse(token, username);
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

        jwtTokenService.invalidateToken(token);
    }
}
