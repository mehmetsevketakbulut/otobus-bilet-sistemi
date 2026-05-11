package com.otobus.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otobus.entity.Ticket;
import com.otobus.entity.Trip;
import com.otobus.entity.TripStop;
import com.otobus.entity.User;
import com.otobus.repository.TicketRepository;
import com.otobus.repository.TripRepository;
import com.otobus.repository.UserRepository;
import com.otobus.dto.request.TicketBuyRequest;
import com.otobus.dto.response.SeatStatusResponse;

/**
 * Bilet servisi.
 * Bilet satın alma, iptal etme, koltuk müsaitlik kontrolü ve
 * bilet listeleme iş mantığını yürütür.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    public TicketService(TicketRepository ticketRepository, TripRepository tripRepository,
                         UserRepository userRepository, AuditLogService auditLogService,
                         NotificationService notificationService, PaymentService paymentService) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
    }

    /**
     * Segment bazlı koltuk müsaitlik durumunu hesapla.
     */
    @Transactional(readOnly = true)
    public List<SeatStatusResponse> getSeatsForSegment(Long tripId, Long fromStopId, Long toStopId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Sefer bulunamadı!"));

        TripStop fromStop = trip.getStops().stream()
                .filter(s -> s.getId().equals(fromStopId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Kalkış durağı bulunamadı!"));

        TripStop toStop = trip.getStops().stream()
                .filter(s -> s.getId().equals(toStopId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Varış durağı bulunamadı!"));

        int reqFromOrder = fromStop.getStopOrder();
        int reqToOrder = toStop.getStopOrder();

        List<Ticket> allTickets = ticketRepository.findByTripId(tripId);
        int seatCapacity = trip.getOtobus().getSeatCapacity();
        List<SeatStatusResponse> seats = new ArrayList<>();

        for (int seatNo = 1; seatNo <= seatCapacity; seatNo++) {
            final int currentSeat = seatNo;

            Ticket overlapping = allTickets.stream()
                    .filter(t -> t.getKoltukNo() == currentSeat)
                    .filter(t -> t.getFromStop().getStopOrder() < reqToOrder
                            && t.getToStop().getStopOrder() > reqFromOrder)
                    .findFirst()
                    .orElse(null);

            if (overlapping != null) {
                seats.add(SeatStatusResponse.builder()
                        .seatNo(currentSeat)
                        .status("occupied")
                        .gender(overlapping.getGender())
                        .build());
            } else {
                seats.add(SeatStatusResponse.builder()
                        .seatNo(currentSeat)
                        .status("available")
                        .gender(null)
                        .build());
            }
        }

        return seats;
    }

    /**
     * Bilet satın alma — Email doğrulama, koltuk çakışma ve TC kontrolü dahil.
     */
    @Transactional
    public Ticket biletKes(TicketBuyRequest request, User user) {
        // Email doğrulama kontrolü (MVC: iş mantığı service'de)
        if (user != null) {
            if (!user.isEmailVerified()) {
                throw new RuntimeException("Bilet alabilmek için e-posta adresinizi doğrulamanız gerekmektedir.");
            }
            request.setUserId(user.getId());
        }

        // Pessimistic Lock ile seferi kilitle
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

        // Koltuk çakışma kontrolü
        List<Ticket> overlapping = ticketRepository.findOverlappingTickets(
                trip.getId(), request.getKoltukNo(),
                fromStop.getStopOrder(), toStop.getStopOrder());

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("HATA: Bu koltuk numarası seçilen güzergah için dolu!");
        }

        // TC kontrolü
        if (request.getTcNo() != null && !request.getTcNo().isEmpty()) {
            List<Ticket> allTicketsForTrip = ticketRepository.findByTripId(trip.getId());
            boolean tcAlreadyBooked = allTicketsForTrip.stream()
                    .anyMatch(t -> request.getTcNo().equals(t.getTcNo()));
            if (tcAlreadyBooked) {
                throw new RuntimeException("Bu T.C. kimlik numarası ile bu sefere zaten bilet alınmış!");
            }
        }

        // Bilet oluştur
        Ticket bilet = new Ticket();
        bilet.setTrip(trip);
        bilet.setFromStop(fromStop);
        bilet.setToStop(toStop);
        bilet.setKoltukNo(request.getKoltukNo());
        bilet.setYolcuAdSoyad(request.getYolcuAdSoyad());
        bilet.setGender(request.getGender());
        bilet.setTcNo(request.getTcNo());

        if (user != null) {
            bilet.setUser(user);
        }

        Ticket savedTicket = ticketRepository.save(bilet);

        // Ödeme kaydı oluştur
        double price = toStop.getPriceFromStart() - fromStop.getPriceFromStart();
        paymentService.createPayment(savedTicket, user, price, "CREDIT_CARD");

        // Audit log
        auditLogService.log(user, "CREATE", "Ticket", savedTicket.getId(),
                "Bilet satın alındı: " + fromStop.getTerminal().getCity().getName() +
                " → " + toStop.getTerminal().getCity().getName() +
                " Koltuk: " + request.getKoltukNo(), null);

        // Bildirim
        if (user != null) {
            notificationService.createNotification(user, "Bilet Satın Alındı",
                    "Biletiniz başarıyla satın alınmıştır. Koltuk No: " + request.getKoltukNo(),
                    "TICKET_PURCHASE");
        }

        return savedTicket;
    }

    /**
     * Bilet iptal etme — Sahiplik kontrolü dahil.
     */
    @Transactional
    public void biletIptalEt(Long ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Bilet bulunamadı!"));

        // Sahiplik kontrolü (ADMIN değilse sadece kendi bileti)
        if (user != null && !user.getRole().name().equals("ADMIN")) {
            if (ticket.getUser() == null || !ticket.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Bu bilet size ait değil!");
            }
        }

        // Ödeme iadesi
        try {
            paymentService.refundPayment(ticketId);
        } catch (Exception e) {
            // Ödeme kaydı yoksa da iptal devam etsin
        }

        ticketRepository.deleteById(ticketId);

        auditLogService.log(user, "DELETE", "Ticket", ticketId,
                "Bilet iptal edildi", null);

        if (user != null) {
            notificationService.createNotification(user, "Bilet İptal Edildi",
                    "Biletiniz başarıyla iptal edilmiştir.", "TICKET_CANCEL");
        }
    }

    public List<Ticket> tumBiletleriGetir() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId);
    }
}