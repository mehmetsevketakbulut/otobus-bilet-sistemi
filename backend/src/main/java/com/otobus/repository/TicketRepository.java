package com.otobus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.otobus.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
       @org.springframework.data.jpa.repository.Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId AND t.koltukNo = :koltukNo "
                     +
                     "AND t.fromStop.stopOrder < :reqToOrder AND t.toStop.stopOrder > :reqFromOrder")
       java.util.List<Ticket> findOverlappingTickets(
                     @org.springframework.data.repository.query.Param("tripId") Long tripId,
                     @org.springframework.data.repository.query.Param("koltukNo") int koltukNo,
                     @org.springframework.data.repository.query.Param("reqFromOrder") int reqFromOrder,
                     @org.springframework.data.repository.query.Param("reqToOrder") int reqToOrder);
}