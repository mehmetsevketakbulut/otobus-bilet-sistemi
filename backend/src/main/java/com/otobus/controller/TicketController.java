package com.otobus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.otobus.entity.Ticket;
import com.otobus.entity.User;
import com.otobus.repository.UserRepository;
import com.otobus.service.TicketService;

/**
 * Bilet controller'ı.
 * Bilet satın alma, listeleme ve iptal işlemlerini yönetir.
 * İş mantığı TicketService'e devredilmiştir (MVC uyumlu).
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyTicket(@RequestBody com.otobus.dto.request.TicketBuyRequest bilet,
                                       Authentication authentication) {
        // Kullanıcı bilgisini al ve service'e ilet (MVC: iş mantığı service'de)
        User user = resolveUser(authentication);
        Ticket savedTicket = ticketService.biletKes(bilet, user);
        return ResponseEntity.ok(savedTicket);
    }

    /**
     * Giriş yapmış kullanıcının biletlerini getir
     */
    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> getMyTickets(Authentication authentication) {
        User user = resolveUser(authentication);
        List<Ticket> tickets = ticketService.getTicketsByUserId(user.getId());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/all")
    public List<Ticket> getAllTickets() {
        return ticketService.tumBiletleriGetir();
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id, Authentication authentication) {
        User user = resolveUser(authentication);
        ticketService.biletIptalEt(id, user);
        return ResponseEntity.ok(Map.of("message", "Bilet başarıyla iptal edildi."));
    }

    /**
     * Authentication'dan kullanıcıyı çözer.
     */
    private User resolveUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Oturum bilgisi bulunamadı!");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
    }
}