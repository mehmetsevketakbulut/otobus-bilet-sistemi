package com.otobus.service;

import com.otobus.entity.Review;
import com.otobus.entity.User;
import com.otobus.entity.Company;
import com.otobus.entity.Trip;
import com.otobus.repository.ReviewRepository;
import com.otobus.repository.CompanyRepository;
import com.otobus.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Değerlendirme servisi.
 * Kullanıcıların seferleri ve firmaları puanlamasını yönetir.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CompanyRepository companyRepository;
    private final TripRepository tripRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         CompanyRepository companyRepository,
                         TripRepository tripRepository) {
        this.reviewRepository = reviewRepository;
        this.companyRepository = companyRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * Yeni değerlendirme oluşturur.
     */
    public Review createReview(User user, Long companyId, Long tripId, int rating, String comment) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Firma bulunamadı!"));

        Review review = new Review();
        review.setUser(user);
        review.setCompany(company);
        review.setRating(rating);
        review.setComment(comment);

        if (tripId != null) {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            review.setTrip(trip);
        }

        return reviewRepository.save(review);
    }

    public List<Review> getCompanyReviews(Long companyId) {
        return reviewRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Double getCompanyAverageRating(Long companyId) {
        return reviewRepository.findAverageRatingByCompanyId(companyId);
    }
}
