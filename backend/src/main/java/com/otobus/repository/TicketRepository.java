package com.otobus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.otobus.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Belirli bir seferde, belirli bir koltuk numarasını bulmaya yarar
    Optional<Ticket> findByTripIdAndKoltukNo(Long tripId, int koltukNo);
}