package com.otobus.repository;

import com.otobus.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.company.id = :companyId")
    Double findAverageRatingByCompanyId(Long companyId);
}
