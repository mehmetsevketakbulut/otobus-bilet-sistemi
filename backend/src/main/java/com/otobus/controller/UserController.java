package com.otobus.controller;

import com.otobus.entity.User;
import com.otobus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /api/users/me — Giriş yapmış kullanıcının kendi profil bilgilerini döner.
     * Token'daki email'i kullanarak DB'den kullanıcıyı çeker.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        Map<String, Object> profile = Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                "role", user.getRole().name());

        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/users/me — Giriş yapmış kullanıcının profil bilgilerini günceller.
     */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateCurrentUser(
            Authentication authentication,
            @RequestBody Map<String, String> updates) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (updates.containsKey("fullName")) {
            user.setFullName(updates.get("fullName"));
        }
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber(updates.get("phoneNumber"));
        }

        userRepository.save(user);

        Map<String, Object> profile = Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                "role", user.getRole().name());

        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/users/all — Tüm kullanıcıları listeler (Sadece ADMIN).
     */
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * DELETE /api/users/{id} — Kullanıcıyı siler (Sadece ADMIN).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Kullanıcı bulunamadı!");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Kullanıcı başarıyla silindi."));
    }
}
