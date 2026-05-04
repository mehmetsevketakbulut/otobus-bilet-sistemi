package com.otobus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.otobus.entity.Ticket;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Segment çakışma kontrolü:
     * İki güzergah [fromA, toA] ve [fromB, toB] çakışır eğer:
     * fromA < toB AND fromB < toA
     * 
     * Örnek: Trabzon(1)→Ankara(3) ile Ankara(3)→Antalya(5)
     * 1 < 5 = true AND 3 < 3 = false → Çakışma YOK → Koltuk müsait!
     */
    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId AND t.koltukNo = :koltukNo " +
            "AND t.fromStop.stopOrder < :reqToOrder AND t.toStop.stopOrder > :reqFromOrder")
    List<Ticket> findOverlappingTickets(
            @Param("tripId") Long tripId,
            @Param("koltukNo") int koltukNo,
            @Param("reqFromOrder") int reqFromOrder,
            @Param("reqToOrder") int reqToOrder);

    /**
     * Bir seferdeki tüm biletleri getir (koltuk durumu hesaplamak için)
     */
    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId")
    List<Ticket> findByTripId(@Param("tripId") Long tripId);

    /**
     * Bir kullanıcının tüm biletlerini getir
     */
    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId ORDER BY t.fromStop.departureTime DESC")
    List<Ticket> findByUserId(@Param("userId") Long userId);
}