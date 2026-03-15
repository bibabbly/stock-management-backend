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

import java.util.List;

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

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // Get permissions from shopRole, or full access if ADMIN
        List<String> permissions = null;
        if (user.getShopRole() != null) {
            permissions = user.getShopRole().getPermissions();
        }

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getShop() != null ? user.getShop().getName() : null,
                user.getId(),
                user.getShop() != null ? user.getShop().getId() : null,
                permissions
        ));
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}
