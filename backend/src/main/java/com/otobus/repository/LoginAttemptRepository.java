package com.otobus.repository;

import com.otobus.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByEmailAndSuccessfulFalseAndAttemptTimeAfter(String email, LocalDateTime after);
    void deleteByAttemptTimeBefore(LocalDateTime before);
}
