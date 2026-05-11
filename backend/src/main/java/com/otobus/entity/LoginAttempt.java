package com.otobus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Giriş denemesi entity'si.
 * Brute-force saldırılarına karşı koruma sağlar.
 * Belirli bir sürede belirli sayıda başarısız giriş denemesi yapılırsa
 * hesap geçici olarak kilitlenir.
 */
@Entity
@Table(name = "login_attempts", indexes = {
    @Index(name = "idx_la_email", columnList = "email"),
    @Index(name = "idx_la_time", columnList = "attemptTime")
})
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private boolean successful;

    @Column(nullable = false, updatable = false)
    private LocalDateTime attemptTime;

    @PrePersist
    protected void onCreate() {
        this.attemptTime = LocalDateTime.now();
    }

    // --- Getter ve Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }

    public LocalDateTime getAttemptTime() { return attemptTime; }
    public void setAttemptTime(LocalDateTime attemptTime) { this.attemptTime = attemptTime; }
}
