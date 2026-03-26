package com.otobus.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.otobus.entity.Ticket;
import com.otobus.repository.TicketRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket biletKes(Ticket bilet) {
        // 1. KONTROL: Bu koltuk aynı sefer için daha önce alınmış mı?
        Optional<Ticket> varMi = ticketRepository.findByTripIdAndKoltukNo(
            bilet.getTrip().getId(), 
            bilet.getKoltukNo()
        );

        if (varMi.isPresent()) {
            // Eğer koltuk doluysa hata fırlat (Böylece aynı koltuk iki kere satılamaz)
            throw new RuntimeException("HATA: Bu koltuk numarası zaten dolu!");
        }

        // 2. Eğer koltuk boşsa bilet kaydını yap
        return ticketRepository.save(bilet);
    }
    // Tüm biletleri listelemek için (Görev: Kullanıcının kendi biletlerini listeleme altyapısı)
    public List<Ticket> tumBiletleriGetir() {
        return ticketRepository.findAll();
    }
    
    // Bilet iptal etmek için (Görev: Bilet iptal etme özelliği)
    public void biletIptalEt(Long id) {
        ticketRepository.deleteById(id);
    }
}