package com.otobus.repository;

import com.otobus.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTicketId(Long ticketId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByStatus(String status);
    Optional<Payment> findByTransactionId(String transactionId);
}
