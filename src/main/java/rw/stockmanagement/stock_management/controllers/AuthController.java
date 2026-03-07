package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.LoginRequest;
import rw.stockmanagement.stock_management.dto.LoginResponse;
import rw.stockmanagement.stock_management.models.User;
import rw.stockmanagement.stock_management.repositories.UserRepository;
import rw.stockmanagement.stock_management.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getShop() != null ? user.getShop().getName() : null,
                user.getId(),
                user.getShop() != null ? user.getShop().getId() : null
        ));
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}
