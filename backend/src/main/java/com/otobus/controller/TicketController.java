package com.otobus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otobus.entity.Ticket;
import com.otobus.entity.User;
import com.otobus.repository.UserRepository;
import com.otobus.service.TicketService;

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
        // Token'dan kullanıcıyı otomatik bul ve userId'yi set et
        if (authentication != null && authentication.getName() != null) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                bilet.setUserId(user.getId());

                // Email doğrulanmamış kullanıcı bilet alamaz
                if (!user.isEmailVerified()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Bilet alabilmek için e-posta adresinizi doğrulamanız gerekmektedir."));
                }
            }
        }

        Ticket savedTicket = ticketService.biletKes(bilet);
        return ResponseEntity.ok(savedTicket);
    }

    /**
     * Giriş yapmış kullanıcının biletlerini getir
     */
    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> getMyTickets(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
        List<Ticket> tickets = ticketService.getTicketsByUserId(user.getId());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/all")
    public List<Ticket> getAllTickets() {
        return ticketService.tumBiletleriGetir();
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id, Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                List<Ticket> userTickets = ticketService.getTicketsByUserId(user.getId());
                boolean ownsTicket = userTickets.stream().anyMatch(t -> t.getId().equals(id));
                if (!ownsTicket) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Bu bilet size ait değil!"));
                }
            }
        }
        ticketService.biletIptalEt(id);
        return ResponseEntity.ok(Map.of("message", "Bilet başarıyla iptal edildi."));
    }
}