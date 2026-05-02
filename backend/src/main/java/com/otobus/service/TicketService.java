package com.otobus.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otobus.entity.Ticket;
import com.otobus.entity.Trip;
import com.otobus.entity.TripStop;
import com.otobus.repository.TicketRepository;
import com.otobus.repository.TripRepository;
import com.otobus.dto.request.TicketBuyRequest;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;

    public TicketService(TicketRepository ticketRepository, TripRepository tripRepository) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional
    public Ticket biletKes(TicketBuyRequest request) {
        // 1. Pessimistic Lock ile seferi kilitle (Aynı anda başka bir işlem bu seferi
        // okuyamaz/yazamaz)
        Trip trip = tripRepository.findByIdForUpdate(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Sefer bulunamadı!"));

        TripStop fromStop = trip.getStops().stream()
                .filter(s -> s.getId().equals(request.getFromStopId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Kalkış durağı bulunamadı!"));

        TripStop toStop = trip.getStops().stream()
                .filter(s -> s.getId().equals(request.getToStopId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Varış durağı bulunamadı!"));

        if (fromStop.getStopOrder() >= toStop.getStopOrder()) {
            throw new RuntimeException("Geçersiz güzergah!");
        }

        // 2. KONTROL: Bu koltuk aynı sefer için kesişen bir güzergahta alınmış mı?
        List<Ticket> overlapping = ticketRepository.findOverlappingTickets(
                trip.getId(),
                request.getKoltukNo(),
                fromStop.getStopOrder(),
                toStop.getStopOrder());

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("HATA: Bu koltuk numarası seçilen güzergah için dolu!");
        }

        // 3. Bilet oluştur ve kaydet
        Ticket bilet = new Ticket();
        bilet.setTrip(trip);
        bilet.setFromStop(fromStop);
        bilet.setToStop(toStop);
        bilet.setKoltukNo(request.getKoltukNo());
        bilet.setYolcuAdSoyad(request.getYolcuAdSoyad());

        Ticket savedTicket = ticketRepository.save(bilet);

        // 4. SMS Gönderim Simülasyonu
        System.out.println("====== SMS GÖNDERİLDİ ======");
        System.out.println("Sayın " + request.getYolcuAdSoyad() + ", biletiniz başarıyla alınmıştır.");
        System.out.println("Sefer: " + fromStop.getTerminal().getCity().getName() + " -> "
                + toStop.getTerminal().getCity().getName());
        System.out.println("Koltuk No: " + request.getKoltukNo());
        System.out.println("============================");

        return savedTicket;
    }

    // Tüm biletleri listelemek için (Görev: Kullanıcının kendi biletlerini
    // listeleme altyapısı)
    public List<Ticket> tumBiletleriGetir() {
        return ticketRepository.findAll();
    }

    // Bilet iptal etmek için (Görev: Bilet iptal etme özelliği)
    public void biletIptalEt(Long id) {
        ticketRepository.deleteById(id);
    }
}