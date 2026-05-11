package com.otobus.controller;

import com.otobus.entity.*;
import com.otobus.repository.*;
import com.otobus.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller'ı.
 * Dashboard istatistikleri, audit logları ve yönetim işlemleri.
 * Sadece ADMIN rolüne sahip kullanıcılar erişebilir.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final TerminalRepository terminalRepository;
    private final AuditLogService auditLogService;

    public AdminController(UserRepository userRepository, CompanyRepository companyRepository,
                           TicketRepository ticketRepository, TripRepository tripRepository,
                           TerminalRepository terminalRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.terminalRepository = terminalRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Dashboard istatistikleri — Gerçek veritabanı verilerinden hesaplanır.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCompanies", companyRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTickets", ticketRepository.count());
        stats.put("totalTrips", tripRepository.count());
        stats.put("totalTerminals", terminalRepository.count());
        return ResponseEntity.ok(stats);
    }

    /**
     * Tüm firmaları listeler.
     */
    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    /**
     * Firmayı günceller.
     */
    @PutMapping("/companies/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Firma bulunamadı!"));
        if (updates.containsKey("name")) {
            company.setName(updates.get("name"));
        }
        companyRepository.save(company);
        return ResponseEntity.ok(company);
    }

    /**
     * Firmayı siler.
     */
    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Map<String, String>> deleteCompany(@PathVariable Long id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Firma bulunamadı!");
        }
        companyRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Firma başarıyla silindi."));
    }

    /**
     * Firma aktif/pasif durumunu değiştirir (toggle).
     */
    @PutMapping("/companies/{id}/toggle-active")
    public ResponseEntity<Company> toggleCompanyActive(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Firma bulunamadı!"));
        company.setActive(!company.isActive());
        companyRepository.save(company);
        return ResponseEntity.ok(company);
    }

    /**
     * Tüm kullanıcıları listeler.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Tüm terminalleri listeler.
     */
    @GetMapping("/terminals")
    public ResponseEntity<List<Terminal>> getAllTerminals() {
        return ResponseEntity.ok(terminalRepository.findAll());
    }

    /**
     * Tüm biletleri listeler.
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    /**
     * Audit log'ları listeler (son 50 kayıt).
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }

    /**
     * Tüm audit log'ları listeler.
     */
    @GetMapping("/audit-logs/all")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}
