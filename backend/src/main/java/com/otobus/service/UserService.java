package com.otobus.service;

import com.otobus.dto.request.LoginRequest;
import com.otobus.dto.request.RegisterRequest;
import com.otobus.dto.response.AuthResponse;
import com.otobus.entity.Company;
import com.otobus.entity.Role;
import com.otobus.entity.User;
import com.otobus.repository.CompanyRepository;
import com.otobus.repository.UserRepository;
import com.otobus.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

        private final UserRepository userRepository;
        private final CompanyRepository companyRepository;
        private final JwtUtil jwtUtil;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;

        public UserService(UserRepository userRepository, CompanyRepository companyRepository, JwtUtil jwtUtil,
                        PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
                this.userRepository = userRepository;
                this.companyRepository = companyRepository;
                this.jwtUtil = jwtUtil;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
        }

        public AuthResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Bu email zaten kayıtlı!");
                }

                Role userRole = Role.USER;
                if (request.getRole() != null) {
                        try {
                                userRole = Role.valueOf(request.getRole().toUpperCase());
                        } catch (Exception e) {
                                userRole = Role.USER;
                        }
                }

                User user = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .phoneNumber(request.getPhoneNumber())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(userRole)
                                .build();

                userRepository.save(user);

                if (userRole == Role.COMPANY) {
                        Company company = new Company();
                        company.setName(request.getFullName() + " Firması");
                        company.setOwner(user);
                        companyRepository.save(company);
                }

                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("role", user.getRole().name());

                String jwtToken = jwtUtil.generateToken(extraClaims, user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .build();
        }

        public AuthResponse login(LoginRequest request) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));
                } catch (org.springframework.security.authentication.BadCredentialsException e) {
                        throw new RuntimeException("Hatalı e-posta veya şifre.");
                } catch (org.springframework.security.core.AuthenticationException e) {
                        throw new RuntimeException("Kimlik doğrulama hatası: " + e.getMessage());
                }

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("role", user.getRole().name());

                String jwtToken = jwtUtil.generateToken(extraClaims, user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .build();
        }
}