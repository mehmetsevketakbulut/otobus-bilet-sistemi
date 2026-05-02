package com.otobus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.otobus.entity.Trip;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    java.util.Optional<Trip> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT t, s1, s2 FROM Trip t JOIN t.stops s1 JOIN t.stops s2 " +
            "WHERE t.isApproved = true " +
            "AND LOWER(s1.terminal.city.name) = LOWER(:fromCity) " +
            "AND LOWER(s2.terminal.city.name) = LOWER(:toCity) " +
            "AND s1.stopOrder < s2.stopOrder " +
            "AND s1.departureTime >= :startOfDay AND s1.departureTime < :endOfDay")
    List<Object[]> findTripsBySearchCriteria(
            @Param("fromCity") String fromCity,
            @Param("toCity") String toCity,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}