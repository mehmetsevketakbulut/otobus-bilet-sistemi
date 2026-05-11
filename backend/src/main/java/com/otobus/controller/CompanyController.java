package com.otobus.controller;

<<<<<<< HEAD
import com.otobus.entity.Company;
import com.otobus.service.CompanyService;
import org.springframework.http.ResponseEntity;
=======
import com.otobus.entity.*;
import com.otobus.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
>>>>>>> 7bb300b (Deployment fix for cloud database)
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

<<<<<<< HEAD
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        return ResponseEntity.ok(companyService.createCompany(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        if (updates.containsKey("name")) {
            return ResponseEntity.ok(companyService.updateCompany(id, updates.get("name")));
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok("Firma başarıyla silindi.");
=======
/**
 * Firma (COMPANY) controller'ı.
 * Firma kullanıcısının kendi otobüslerini, seferlerini ve istatistiklerini
 * yönetmesini sağlar. Sadece COMPANY rolüne sahip kullanıcılar erişebilir.
 */
@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public CompanyController(CompanyRepository companyRepository, BusRepository busRepository,
                             TripRepository tripRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.busRepository = busRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    /**
     * Giriş yapmış firma kullanıcısının firma bilgisini döner.
     */
    @GetMapping("/me")
    public ResponseEntity<Company> getMyCompany(Authentication authentication) {
        Company company = resolveCompany(authentication);
        return ResponseEntity.ok(company);
    }

    /**
     * Firmanın kendi otobüslerini listeler.
     */
    @GetMapping("/buses")
    public ResponseEntity<List<Bus>> getMyBuses(Authentication authentication) {
        Company company = resolveCompany(authentication);
        List<Bus> buses = busRepository.findByCompanyId(company.getId());
        return ResponseEntity.ok(buses);
    }

    /**
     * Firmanın kendi seferlerini listeler (onaylı + onay bekleyen).
     */
    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        Company company = resolveCompany(authentication);
        List<Bus> companyBuses = busRepository.findByCompanyId(company.getId());
        List<Long> busIds = companyBuses.stream().map(Bus::getId).toList();

        List<Trip> trips = tripRepository.findAll().stream()
                .filter(t -> busIds.contains(t.getOtobus().getId()))
                .toList();

        return ResponseEntity.ok(trips);
    }

    /**
     * Firma dashboard istatistikleri.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(Authentication authentication) {
        Company company = resolveCompany(authentication);
        List<Bus> companyBuses = busRepository.findByCompanyId(company.getId());
        List<Long> busIds = companyBuses.stream().map(Bus::getId).toList();

        long totalTrips = tripRepository.findAll().stream()
                .filter(t -> busIds.contains(t.getOtobus().getId()))
                .count();

        long approvedTrips = tripRepository.findAll().stream()
                .filter(t -> busIds.contains(t.getOtobus().getId()) && t.isApproved())
                .count();

        long pendingTrips = totalTrips - approvedTrips;

        Map<String, Object> stats = Map.of(
                "companyName", company.getName(),
                "totalBuses", companyBuses.size(),
                "totalTrips", totalTrips,
                "approvedTrips", approvedTrips,
                "pendingTrips", pendingTrips
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * Authentication'dan firma bilgisini çözer.
     */
    private Company resolveCompany(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Oturum bilgisi bulunamadı!");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        return companyRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new RuntimeException("Bu kullanıcıya ait firma bulunamadı!"));
>>>>>>> 7bb300b (Deployment fix for cloud database)
    }
}
