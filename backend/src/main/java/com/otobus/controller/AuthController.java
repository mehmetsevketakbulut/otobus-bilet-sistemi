package com.otobus.controller;

import com.otobus.dto.request.LoginRequest;
import com.otobus.dto.request.RegisterRequest;
import com.otobus.dto.response.AuthResponse;
import com.otobus.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * E-posta doğrulama kodu kontrolü
     */
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        return ResponseEntity.ok(userService.verifyEmail(email, code));
    }

    /**
     * Doğrulama kodunu yeniden gönder
     */
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.resendVerificationCode(email);
        return ResponseEntity.ok(Map.of("message", "Doğrulama kodu tekrar gönderildi."));
    }
}
