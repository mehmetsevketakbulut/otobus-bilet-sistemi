package com.otobus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Sefer/Firma değerlendirme entity'si.
 * Kullanıcılar tamamlanmış seferleri puanlayabilir ve yorum bırakabilir.
 * Firma kalitesini ölçmek ve diğer kullanıcılara rehberlik etmek için kullanılır.
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_company", columnList = "company_id"),
    @Index(name = "idx_review_user", columnList = "user_id")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private int rating; // 1-5 arası puan

    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getter ve Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public int getRating() { return rating; }
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Puan 1-5 arasında olmalıdır.");
        }
        this.rating = rating;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
