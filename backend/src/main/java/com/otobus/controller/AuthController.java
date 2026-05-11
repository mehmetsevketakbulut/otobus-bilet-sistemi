package com.otobus.controller;

import com.otobus.dto.request.LoginRequest;
import com.otobus.dto.request.RegisterRequest;
import com.otobus.dto.response.AuthResponse;
import com.otobus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Kimlik doğrulama controller'ı.
 * Kayıt, giriş, e-posta doğrulama ve şifre sıfırlama işlemlerini yönetir.
 * İş mantığı tamamen UserService'de yer alır (MVC uyumlu).
 */
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
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(userService.login(request, ipAddress));
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

    /**
     * Şifre sıfırlama isteği — Kullanıcıya e-posta ile doğrulama kodu gönderir.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "Şifre sıfırlama kodu e-posta adresinize gönderildi."));
    }

    /**
     * Şifre sıfırlama kodu doğrulama
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        userService.verifyPasswordResetCode(email, code);
        return ResponseEntity.ok(Map.of("message", "Doğrulama kodu geçerli."));
    }

    /**
     * Yeni şifre belirleme
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");
        userService.resetPassword(email, code, newPassword, confirmPassword);
        return ResponseEntity.ok(Map.of("message", "Şifreniz başarıyla değiştirildi."));
    }

    /**
     * İstemci IP adresini tespit eder (proxy arkasında bile çalışır).
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
