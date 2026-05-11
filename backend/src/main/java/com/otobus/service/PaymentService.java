package com.otobus.service;

import com.otobus.entity.Payment;
import com.otobus.entity.Ticket;
import com.otobus.entity.User;
import com.otobus.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Ödeme servisi.
 * Bilet satın alma sürecinde ödeme kaydı oluşturur,
 * iade işlemlerini ve ödeme geçmişini yönetir.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Yeni ödeme kaydı oluşturur.
     */
    public Payment createPayment(Ticket ticket, User user, double amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("COMPLETED");
        payment.setTransactionId(generateTransactionId());
        return paymentRepository.save(payment);
    }

    /**
     * Bilet iptalinde ödeme iadesini gerçekleştirir.
     */
    public Payment refundPayment(Long ticketId) {
        Payment payment = paymentRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ödeme kaydı bulunamadı!"));
        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }

    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Benzersiz işlem ID'si üretir.
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
