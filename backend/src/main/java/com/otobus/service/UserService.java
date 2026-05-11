package com.otobus.service;

import com.otobus.dto.request.LoginRequest;
import com.otobus.dto.request.RegisterRequest;
import com.otobus.dto.response.AuthResponse;
import com.otobus.entity.*;
import com.otobus.repository.*;
import com.otobus.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

/**
 * Kullanıcı servisi.
 * Kayıt, giriş, email doğrulama, şifre sıfırlama, brute-force koruması
 * ve kullanıcı yönetimi işlemlerini yürütür.
 */
@Service
public class UserService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    private static final long JWT_REMEMBER_ME_EXPIRATION = 604800000L; // 7 gün
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository,
                       LoginAttemptRepository loginAttemptRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       EmailService emailService, AuditLogService auditLogService,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    /**
     * 6 haneli rastgele doğrulama kodu üretir.
     */
    private String generateVerificationCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Şifre gücü validasyonu.
     * En az 1 büyük harf, 1 rakam ve 1 özel karakter (. ? !) gerektirir.
     * Minimum 8 karakter uzunluk.
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Şifre en az 8 karakter olmalıdır.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("Şifre en az 1 büyük harf içermelidir.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException("Şifre en az 1 rakam içermelidir.");
        }
        if (!password.matches(".*[.?!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,<>/~`].*")) {
            throw new RuntimeException("Şifre en az 1 özel karakter içermelidir (. ? ! @ # vb.).");
        }
    }

    /**
     * Brute-force kontrolü: Son 15 dakikada 5+ başarısız giriş varsa hesabı kilitler.
     */
    private void checkBruteForce(String email) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);
        List<LoginAttempt> recentFails = loginAttemptRepository
                .findByEmailAndSuccessfulFalseAndAttemptTimeAfter(email, cutoff);

        if (recentFails.size() >= MAX_LOGIN_ATTEMPTS) {
            throw new RuntimeException(
                    "Çok fazla başarısız giriş denemesi. Hesabınız " + LOCKOUT_MINUTES +
                    " dakika süreyle kilitlenmiştir. Lütfen daha sonra tekrar deneyin.");
        }
    }

    /**
     * Giriş denemesini kaydeder (başarılı veya başarısız).
     */
    private void recordLoginAttempt(String email, String ipAddress, boolean successful) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(successful);
        loginAttemptRepository.save(attempt);
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email zaten kayıtlı!");
        }

        // Şifre gücü kontrolü
        validatePasswordStrength(request.getPassword());

        Role userRole = Role.USER;
        if (request.getRole() != null) {
            try {
                userRole = Role.valueOf(request.getRole().toUpperCase());
            } catch (Exception e) {
                userRole = Role.USER;
            }
        }

        // Doğrulama kodu üret
        String verificationCode = generateVerificationCode();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .emailVerified(false)
                .verificationCode(verificationCode)
                .verificationCodeExpiry(LocalDateTime.now().plusMinutes(10))
                .build();

        userRepository.save(user);

        // Firma rolü ise firma oluştur
        if (userRole == Role.COMPANY) {
            Company company = new Company();
            company.setName(request.getFullName() + " Firması");
            company.setOwner(user);
            companyRepository.save(company);
        }

        // Doğrulama mailini gönder
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (Exception e) {
            System.err.println("E-posta gönderilemedi: " + e.getMessage());
        }

        // Audit log
        auditLogService.log(user, "CREATE", "User", user.getId(),
                "Yeni kullanıcı kaydı: " + user.getEmail(), null);

        // Hoş geldin bildirimi
        notificationService.createNotification(user, "Hoş Geldiniz!",
                "OtoBilet'e hoş geldiniz. E-posta adresinizi doğrulamayı unutmayın.", "WELCOME");

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("emailVerified", false);
        extraClaims.put("userId", user.getId());

        String jwtToken = jwtUtil.generateToken(extraClaims, user);
        return AuthResponse.builder()
                .token(jwtToken)
                .emailVerified(false)
                .build();
    }

    /**
     * Email doğrulama: kullanıcının girdiği kodu kontrol eder.
     */
    public AuthResponse verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("E-posta zaten doğrulanmış.");
        }

        if (user.getVerificationCode() == null || user.getVerificationCodeExpiry() == null) {
            throw new RuntimeException("Doğrulama kodu bulunamadı. Lütfen yeni kod isteyin.");
        }

        if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            throw new RuntimeException("Doğrulama kodunun süresi dolmuş. Lütfen yeni kod isteyin.");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Doğrulama kodu hatalı!");
        }

        // Doğrulama başarılı
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        auditLogService.log(user, "UPDATE", "User", user.getId(),
                "E-posta doğrulandı: " + user.getEmail(), null);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("emailVerified", true);
        extraClaims.put("userId", user.getId());

        String jwtToken = jwtUtil.generateToken(extraClaims, user);
        return AuthResponse.builder()
                .token(jwtToken)
                .emailVerified(true)
                .build();
    }

    /**
     * Doğrulama kodunu yeniden gönder.
     */
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("E-posta zaten doğrulanmış.");
        }

        String newCode = generateVerificationCode();
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), newCode);
    }

    /**
     * Giriş işlemi — Brute-force koruması ve "Beni Hatırla" destekli.
     */
    public AuthResponse login(LoginRequest request, String ipAddress) {
        // Brute-force kontrolü
        checkBruteForce(request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Başarısız giriş kaydı
            recordLoginAttempt(request.getEmail(), ipAddress, false);
            auditLogService.logAnonymous("LOGIN_FAILED", "User",
                    "Başarısız giriş: " + request.getEmail(), ipAddress);
            throw new RuntimeException("Hatalı e-posta veya şifre.");
        } catch (org.springframework.security.core.AuthenticationException e) {
            recordLoginAttempt(request.getEmail(), ipAddress, false);
            throw new RuntimeException("Kimlik doğrulama hatası.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Başarılı giriş kaydı
        recordLoginAttempt(request.getEmail(), ipAddress, true);
        auditLogService.log(user, "LOGIN", "User", user.getId(),
                "Başarılı giriş", ipAddress);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("emailVerified", user.isEmailVerified());
        extraClaims.put("userId", user.getId());

        // "Beni Hatırla" aktifse 7 günlük token üret
        long expiration = (request.isRememberMe()) ? JWT_REMEMBER_ME_EXPIRATION : 0;
        String jwtToken;
        if (expiration > 0) {
            jwtToken = jwtUtil.generateToken(extraClaims, user, expiration);
        } else {
            jwtToken = jwtUtil.generateToken(extraClaims, user);
        }

        return AuthResponse.builder()
                .token(jwtToken)
                .emailVerified(user.isEmailVerified())
                .build();
    }

    /**
     * Eski login metodu (geriye dönük uyumluluk).
     */
    public AuthResponse login(LoginRequest request) {
        return login(request, null);
    }

    // ========== ŞİFRE SIFIRLAMA ==========

    /**
     * Şifre sıfırlama isteği — Kullanıcıya e-posta ile doğrulama kodu gönderir.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu e-posta adresiyle kayıtlı kullanıcı bulunamadı."));

        // Eski tokenleri sil (aynı anda sadece 1 geçerli kod olsun)
        passwordResetTokenRepository.deleteByEmail(email);

        String code = generateVerificationCode();

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setEmail(email);
        token.setCode(code);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(email, code);

        auditLogService.log(user, "PASSWORD_RESET", "User", user.getId(),
                "Şifre sıfırlama kodu gönderildi", null);
    }

    /**
     * Şifre sıfırlama kodu doğrulama.
     */
    public void verifyPasswordResetCode(String email, String code) {
        PasswordResetToken token = passwordResetTokenRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Geçersiz doğrulama kodu!"));

        if (!token.isValid()) {
            throw new RuntimeException("Doğrulama kodunun süresi dolmuş. Lütfen yeni kod isteyin.");
        }
    }

    /**
     * Şifre sıfırlama — Doğrulama kodu onaylandıktan sonra yeni şifre belirlenir.
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword, String confirmPassword) {
        // Şifre eşleşme kontrolü
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Şifreler eşleşmiyor!");
        }

        // Şifre gücü kontrolü
        validatePasswordStrength(newPassword);

        PasswordResetToken token = passwordResetTokenRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Geçersiz doğrulama kodu!"));

        if (!token.isValid()) {
            throw new RuntimeException("Doğrulama kodunun süresi dolmuş.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token'ı kullanıldı olarak işaretle
        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        auditLogService.log(user, "PASSWORD_RESET", "User", user.getId(),
                "Şifre başarıyla sıfırlandı", null);

        notificationService.createNotification(user, "Şifre Değiştirildi",
                "Hesap şifreniz başarıyla değiştirildi. Bu işlemi siz yapmadıysanız lütfen bizimle iletişime geçin.",
                "PASSWORD_RESET");
    }
}